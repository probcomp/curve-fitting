(ns curve-fitting.system
  (:require [integrant.core :as integrant]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.core :as core]))

(def config
  {:state {}
   :sketch {:state (integrant/ref :state)}
   :engine {:state (integrant/ref :state)}})

(defmethod integrant/init-key :engine
  [_ {:keys [state]}]
  (let [stop? (promise)]
    (dotimes [_ 4]
      (future
        (try
          (loop []
            (when-not (realized? stop?)
              (let [{old-points :points :as old-val} @state
                    curve (core/points-to-curve old-points)]
                (swap! state (fn [{new-points :points :as new-val}]
                               ;; Discard the curve if the points changed while
                               ;; we were working.
                               (if-not (= new-points old-points)
                                 new-val
                                 (update new-val :curves conj curve)))))
              (recur)))
          (catch Exception e
            (.printStackTrace e))))
      (Thread/sleep 250))
    stop?))

(defmethod integrant/halt-key! :engine
  [_ stop?]
  (deliver stop? true))

(defmethod integrant/init-key :state
  [_ props]
  (atom (core/init)))

(defmethod integrant/init-key :sketch
  [_ {:keys [state]}]
  (applet/applet :size [core/pixel-width core/pixel-height]
                 :draw (core/draw! state)
                 :mouse-pressed (core/mouse-pressed state)
                 :key-typed (core/key-typed state)
                 ;; Why :no-bind-output is necessary: https://github.com/quil/quil/issues/216
                 :features [:keep-on-top :no-bind-output]
                 :middleware [#'middleware/fun-mode]
                 :settings #'core/settings))

(defmethod integrant/halt-key! :sketch
  [_ sketch]
  (.exit sketch))
