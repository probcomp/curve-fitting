(ns equations.server
  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults]
   [ring.middleware.anti-forgery :as anti-forgery]
   [compojure.core     :as comp :refer (defroutes GET POST)]
   [compojure.route    :as route]
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
   [taoensso.encore    :as encore :refer (have have?)]
   [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
   [taoensso.sente     :as sente]
   [integrant.core     :as ig]
   [org.httpkit.server :as http-kit]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

;; (timbre/set-level! :trace) ; Uncomment for more logging
(reset! sente/debug-mode?_ true) ; Uncomment for extra debug info

(def run-equation (atom false))

;;;; Define our Sente channel socket (chsk) server

(let [packer :edn
      chsk-server (sente/make-channel-socket-server!
                   (get-sch-adapter) {:packer packer})
      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]

  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

;; We can watch this atom for changes if we like
(add-watch connected-uids :connected-uids
  (fn [_ _ old new]
    (when (not= old new)
      (infof "Connected uids change: %s" new))))

;;;; Ring handlers

(defn landing-pg-handler [ring-req]
  (hiccup/html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:meta {:name "description" :content ""}]
    [:title "Equations"]
    [:style {:id "style-container" :type "text/css"}]

    [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/icon?family=Material+Icons"}]
    [:link {:rel "stylesheet" :href "https://code.getmdl.io/1.3.0/material.green-blue.min.css"}]
    [:script {:src "https://code.getmdl.io/1.3.0/material.min.js" :defer true}]]
   [:body
    [:div#sente-csrf-token {:data-csrf-token anti-forgery/*anti-forgery-token*}]
    [:div {:class "mdl-layout mdl-js-layout"}
     [:div {:id "app"}]]
    [:script {:src "js/out/canvas.js"}]]))

(defroutes ring-routes
  (GET  "/"      ring-req (landing-pg-handler            ring-req))
  (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
  (route/resources "/") ; Static files, notably public/main.js (our cljs target)
  (route/not-found "<h1>Page not found</h1>"))

(def main-ring-handler
  (ring.middleware.defaults/wrap-defaults
    ring-routes ring.middleware.defaults/site-defaults))

(defn start-equation-broadcaster!
  "Send a new equation to each connected client every 500ms"
  []
  (if (true? @run-equation)
    (debugf "Equation loop already running")
    (do
      (reset! run-equation true)
      (let [broadcast! (fn []
                         (let [uids (:any @connected-uids)]
                           (doseq [uid uids]
                             (chsk-send! uid [:equation/new {:a (- 2 (rand 4))
                                                             :b (+ 1 (rand-int 3))
                                                             :c (- 5 (rand 10))}]))))]
        (go-loop []
          (<! (async/timeout 500))
          (broadcast!)
          (when (true? @run-equation)
            (recur)))))))

;;;; Sente event handlers

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg) ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (let [session (:session ring-req)
        uid     (:uid     session)]
    (debugf "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

;;;; Sente event router (our `event-msg-handler` loop)

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-fn @router_] (stop-fn)))
(defn start-router! []
  (stop-router!)
  (reset! router_
    (sente/start-server-chsk-router!
      ch-chsk event-msg-handler)))

;;;; Init stuff

(defonce    web-server_ (atom nil))
(defn  stop-web-server! [] (when-let [stop-fn @web-server_] (stop-fn)))
(defn start-web-server! [& [port]]
  (stop-web-server!)
  (let [port (or port 0) ; 0 => Choose any available port
        ring-handler (var main-ring-handler)

        [port stop-fn] (let [stop-fn (http-kit/run-server ring-handler {:port port})]
                         [(:local-port (meta stop-fn)) (fn [] (stop-fn :timeout 100))])
        uri (format "http://localhost:%s/" port)]

    (infof "Web server is running at `%s`" uri)

    (reset! web-server_ stop-fn)))

(def config
  {:adapter/http-kit {:port 3333, :handler (ig/ref :handler/equations)}
   :handler/equations {}})

(defmethod ig/init-key :adapter/http-kit [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (dissoc opts :handler)]
    {:handler     handler
     :stop-server (http-kit/run-server (fn [req] (@@handler req)) options)}))

(defmethod ig/init-key :handler/equations [_ _]
  main-ring-handler)

(defmethod ig/halt-key! :adapter/http-kit [_ {:keys [stop-server]}]
  (stop-server))


(defn stop!  []  (stop-router!) (reset! run-equation false))
(defn start! [] (start-router!) (start-equation-broadcaster!))

(comment
  (def system
    (ig/init config))
  (ig/halt! system)


  (start!)
  (stop!)


  (start-web-server! 3333)
  (stop-web-server!)
  )
