(ns equations.ws
  (:require
   [equations.channels :as channels]
   [taoensso.sente :as sente]
   [taoensso.encore :as encore :refer-macros (have have?)]
   [cljs.core.async :refer [put!]]
   [cljs.core.async :refer-macros [go]]))

(defn configure-chsk [csrf-token]
  (let [rand-chsk-type :auto
        packer :edn
        {:keys [chsk ch-recv send-fn state]} (sente/make-channel-socket-client!
                                              "/chsk"
                                              csrf-token
                                              {:type   rand-chsk-type
                                               :packer packer})]

    (def chsk       chsk)

    ;; ChannelSocket's receive channel
    (def ch-chsk    ch-recv)

    ;; ChannelSocket's send API fn
    (def chsk-send! send-fn)

    ;; Watchable, read-only atom
    (def chsk-state state)))

(defmulti -event-msg-handler
  "Multimethod to handle Sente `event-msg`s"
  :id)

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg))

(defmethod -event-msg-handler
  :default ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event]}]
  (.log js/console "Unhandled event: %s" event))

(defmethod -event-msg-handler :chsk/state
  [{:as ev-msg :keys [?data]}]
  (let [[old-state-map new-state-map] (have vector? ?data)]
    (if (:first-open? new-state-map)
      (.log js/console "Channel socket successfully established!: %s" new-state-map)
      (.log js/console "Channel socket state change: %s"              new-state-map))))

(defmethod -event-msg-handler :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (let [[msg-type msg] ?data]
    (.log js/console "Push event from server: %s" ?data)
    (case msg-type
      :equation/new (put! channels/equation-channel ((juxt :a :b :c) msg)))))

(defmethod -event-msg-handler :chsk/handshake
  [{:as ev-msg :keys [?data]}]
  (let [[?uid ?csrf-token ?handshake-data] ?data]
    (.log js/console "Handshake: %s" ?data)))


(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (stop-router!)
  (reset! router_
          (sente/start-client-chsk-router! ch-chsk event-msg-handler)))
