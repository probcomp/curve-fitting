(ns equations.server
  (:require
   [clojure.string     :as str]
   [ring.middleware.defaults]
   [ring.middleware.anti-forgery :as anti-forgery]
   [compojure.core     :as comp :refer (routes defroutes GET POST)]
   [compojure.route    :as route]
   [hiccup.core        :as hiccup]
   [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
   [taoensso.encore    :as encore :refer (have have?)]
   [taoensso.timbre    :as timbre :refer (tracef debugf infof warnf errorf)]
   [taoensso.sente     :as sente]
   [integrant.core     :as ig]
   [org.httpkit.server :as http-kit]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

(reset! sente/debug-mode?_ true) ; Uncomment for extra debug info

;; Ring handlers, for normal HTTP activity

(defn landing-pg-handler [ring-req]
  (hiccup/html
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:meta {:name "description" :content ""}]
    [:title "Equations"]
    [:style {:id "style-container" :type "text/css"}]
    [:link {:rel "stylesheet"
            :href "https://fonts.googleapis.com/icon?family=Material+Icons"}]
    [:link {:rel "stylesheet"
            :href "https://code.getmdl.io/1.3.0/material.green-blue.min.css"}]
    [:script {:src "https://code.getmdl.io/1.3.0/material.min.js" :defer true}]]
   [:body
    [:div#sente-csrf-token {:data-csrf-token anti-forgery/*anti-forgery-token*}]
    [:div {:class "mdl-layout mdl-js-layout"}
     [:div {:id "app"}]]
    [:script {:src "js/out/canvas.js"}]]))

(defn make-ring-handler [{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]}]
  (let [ring-routes (routes
                     (GET  "/"      ring-req (landing-pg-handler            ring-req))
                     (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
                     (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
                     (route/resources "/")
                     (route/not-found "<h1>Page not found</h1>"))]
    (ring.middleware.defaults/wrap-defaults
     ring-routes ring.middleware.defaults/site-defaults)))

;; start the loop which streams equations to the client

(defn start-equation-broadcaster!
  "Send a new equation to each connected client every 500ms"
  [connected-uids-atom chsk-send!]
  (let [run-equation (atom true)
        broadcast!  (fn []
                      (let [uids (:any @connected-uids-atom)]
                        (doseq [uid uids]
                          (println "Hello uid" uid)
                          (chsk-send! uid [:equation/new {:a (- 2 (rand 4))
                                                          :b (+ 1 (rand-int 3))
                                                          :c (- 5 (rand 10))}]))))]
    (go-loop []
      (<! (async/timeout 500))
      (broadcast!)
      (when (true? @run-equation)
        (recur)))

    run-equation))

;; Sente event handlers (client->server, not really used here)

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

;; integrant setup

(def config
  {:adapter/http-kit {:port 3333, :handler (ig/ref :handler/equations)}
   :channelsocket/event-msg-handler {}
   :channelsocket/channelsocket {}
   :channelsocket/router {:channelsocket (ig/ref :channelsocket/channelsocket)
                          :handler (ig/ref :channelsocket/event-msg-handler)}
   :channelsocket/equation-broadcaster {:channelsocket (ig/ref :channelsocket/channelsocket)}

   :handler/equations {:channelsocket (ig/ref :channelsocket/channelsocket)}})

(defmethod ig/init-key :handler/equations [_ {:keys [channelsocket]}]
  (make-ring-handler channelsocket))


(defmethod ig/init-key :adapter/http-kit [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (dissoc opts :handler)]
    {:handler     handler
     :stop-server (http-kit/run-server (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! :adapter/http-kit [_ {:keys [stop-server]}]
  (stop-server))

(defmethod ig/suspend-key! :adapter/http-kit [_ opts]
  opts)

(defmethod ig/resume-key :adapter/http-kit [key opts old-opts old-impl]
  old-impl)

(defmethod ig/init-key :channelsocket/channelsocket [_ opts]
  (let [chsk-server (sente/make-channel-socket-server!
                     (get-sch-adapter) {:packer :edn})
        {:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
    {:ring-ajax-post                ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
     :ch-chsk                       ch-recv ;; channelsocket receive channel
     :chsk-send!                    send-fn ;; channelsocket send fn
     :connected-uids                connected-uids ;; Watchable, read-only atom
     }))


(defmethod ig/suspend-key! :channelsocket/channelsocket [_ opts]
  opts)

(defmethod ig/resume-key :channelsocket/channelsocket [key opts old-opts old-impl]
  old-impl)

(defmethod ig/init-key :channelsocket/router [_ {:keys [handler channelsocket]}]
  (sente/start-server-chsk-router!
   (:ch-chsk channelsocket) handler))

(defmethod ig/init-key :channelsocket/event-msg-handler [_ _]
  event-msg-handler)

(defmethod ig/init-key :channelsocket/equation-broadcaster [_ {:keys [channelsocket]}]
  {:run-atom (start-equation-broadcaster! (:connected-uids channelsocket)
                                          (:chsk-send! channelsocket))})

(defmethod ig/halt-key! :channelsocket/equation-broadcaster [_ {:keys [run-atom]}]
  (reset! run-atom false))

(comment

  (def system
    (ig/init config))

  (ig/suspend! system)

  (def system
    (ig/resume config system))

  (ig/halt! system)

  )
