(ns equations.server
  (:require
   [clojure.core.async :as async  :refer [<! <!! >! >!! alts! put! chan go go-loop]]
   [clojure.string :as str]
   [compojure.core :as compojure :refer [defroutes GET POST DELETE]]
   [compojure.route :as route]
   [figwheel-sidecar.repl-api :as figwheel]
   [hiccup.core :as hiccup]
   [integrant.core :as ig]
   [org.httpkit.server :as http-kit]
   [ring.middleware.anti-forgery :as anti-forgery]
   [ring.middleware.defaults :refer [wrap-defaults]]
   [ring.middleware.format :refer [wrap-restful-format]]
   [taoensso.encore :as encore :refer [have have?]]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]
   [taoensso.timbre :as timbre :refer [tracef debugf infof warnf errorf]]
   [equations.curve :as curve]))

(reset! sente/debug-mode?_ true) ; Uncomment for extra debug info

;; Ring handlers, for normal HTTP activity

(defn landing-pg-handler
  [ring-req]
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

;; start the loop which streams equations to the client

(defn start-equation-broadcaster!
  "Send a new equation to each connected client every 500ms"
  [connected-uids-atom chsk-send!]
  (let [run-equation (atom true)
        broadcast!  (fn []
                      (let [uids (:any @connected-uids-atom)]
                        (doseq [uid uids]
                          (chsk-send! uid [:equation/new {:a (- 2 (rand 4))
                                                          :b (+ 1 (rand-int 3))
                                                          :c (- 5 (rand 10))}]))))]
    (go-loop []
      (<! (async/timeout 100))
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

(defmethod ig/init-key :point-channel
  [_ opts]
  (async/chan (async/sliding-buffer 10)))

(defmethod ig/halt-key! :point-channel
  [_ channel]
  (async/close! channel))

(defmethod ig/init-key :point-handler
  [_ {:keys [channel]}]
  (fn point-handler
    [{:keys [params] :as request}]
    (if (put! channel [:new-point params])
      {:status 200, :body "ok"}
      {:status 500, :body "point channel closed"})))

(defmethod ig/init-key :clear-points-handler
  [_ {:keys [channel]}]
  (fn clear-points-handler
    [_]
    (if (put! channel [:clear-points])
      {:status 200, :body "ok"}
      {:status 500, :body "point channel closed"})))

(def config
  {:sente-socket-server {}
   :sente-router {:channelsocket (ig/ref :sente-socket-server)}
   :sente-out-channel {:sente-socket-server (ig/ref :sente-socket-server)}

   :curve-generator {:sente-out-channel (ig/ref :sente-out-channel)
                     :point-channel (ig/ref :point-channel)}

   :point-channel {}
   :point-handler {:channel (ig/ref :point-channel)}
   :clear-points-handler {:channel (ig/ref :point-channel)}
   :handler {:channelsocket (ig/ref :sente-socket-server)
             :point-handler (ig/ref :point-handler)
             :clear-points-handler (ig/ref :clear-points-handler)}

   :web-server {:port 3333, :handler (ig/ref :handler)}})

(defmethod ig/init-key :handler
  [_ {:keys [point-handler channelsocket clear-points-handler]}]
  (let [{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} channelsocket]
    (let [wrapped-point-handler (wrap-restful-format point-handler)
          wrapped-clear-points-handler (wrap-restful-format clear-points-handler)
          ring-routes (compojure/routes
                       (GET    "/"       request (landing-pg-handler request))
                       (GET    "/chsk"   request (ring-ajax-get-or-ws-handshake request))
                       (POST   "/chsk"   request (ring-ajax-post request))
                       (POST   "/point"  request (wrapped-point-handler request))
                       (DELETE "/points" request (wrapped-clear-points-handler request))
                       (route/resources "/")
                       (route/not-found "<h1>Page not found</h1>"))]
      (wrap-defaults ring-routes ring.middleware.defaults/api-defaults))))

(defmethod ig/init-key :web-server
  [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (dissoc opts :handler)]
    {:handler     handler
     :stop-server (http-kit/run-server (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! :web-server
  [_ {:keys [stop-server]}]
  (stop-server))

(defmethod ig/suspend-key! :web-server
  [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resume-key :web-server
  [key opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))

(defmethod ig/init-key :sente-socket-server
  [_ _]
  (let [chsk-server (sente/make-channel-socket-server!
                     (get-sch-adapter)
                     {:packer :edn})
        {:keys [ch-recv send-fn connected-uids
                ajax-post-fn ajax-get-or-ws-handshake-fn]} chsk-server]
    {:ring-ajax-post                ajax-post-fn
     :ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn
     :ch-chsk                       ch-recv ;; channelsocket receive channel
     :chsk-send!                    send-fn ;; channelsocket send fn
     :connected-uids                connected-uids})) ;; Watchable, read-only atom

(defmethod ig/suspend-key! :sente-socket-server
  [_ opts]
  opts)

(defmethod ig/resume-key :sente-socket-server
  [key opts old-opts old-impl]
  old-impl)

(defmethod ig/init-key :sente-out-channel
  [_ {:keys [sente-socket-server]}]
  (let [c (async/chan (async/dropping-buffer 1))
        {:keys [connected-uids chsk-send!]} sente-socket-server]
    (go-loop []
      (let [x (<! c)]
        (when (some? x)
          (doseq [uid (:any @connected-uids)]
            (chsk-send! uid x))
          (recur))))
    c))

(defmethod ig/halt-key! :sente-out-channel
  [_ stop-chan]
  (async/close! stop-chan))

(defmethod ig/suspend-key! :sente-out-channel
  [_ _])

(defmethod ig/resume-key :sente-out-channel
  [_ _ _ old-impl]
  old-impl)

(defmethod ig/init-key :sente-router
  [_ {:keys [handler channelsocket]}]
  (sente/start-server-chsk-router!
   (:ch-chsk channelsocket) handler))

(defmethod ig/init-key :channelsocket/equation-broadcaster
  [_ {:keys [channelsocket]}]
  {:run-atom (start-equation-broadcaster! (:connected-uids channelsocket)
                                          (:chsk-send! channelsocket))})

(defmethod ig/halt-key! :channelsocket/equation-broadcaster
  [_ {:keys [run-atom]}]
  (reset! run-atom false))


(defmethod ig/init-key :curve-generator
  [_ {:keys [sente-out-channel point-channel]}]
  (curve/start-loop point-channel sente-out-channel))

(defmethod ig/halt-key! :curve-generator
  [_ stop-chan]
  (async/close! stop-chan))
