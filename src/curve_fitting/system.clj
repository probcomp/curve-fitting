(ns curve-fitting.system
  (:require [integrant.core :as integrant]
            [quil.core :as quil]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.core :as core]
            [curve-fitting.db :as db]
            [curve-fitting.scales :as scales]
            [curve-fitting.sketches.resampling :as resampling]))

(def config
  {:state {}
   :sketch {:state (integrant/ref :state)
            :x-point-min -10
            :x-point-max 10
            :pixel-width 500
            :pixel-height 500
            :y-point-min -10
            :y-point-max 10
            :anti-aliasing 8}
   :engine {:state (integrant/ref :state)
            :num-particles 150}})

(defmethod integrant/init-key :engine
  [_ {:keys [num-particles state]}]
  (let [stop? (atom false)]
    (dotimes [_ 4]
      (future
        (try
          (loop []
            (when-not @stop?
              (let [{old-points :points :as old-val} @state
                    curve (resampling/points-to-curve num-particles old-points)]
                (swap! state (fn [{new-points :points :as new-val}]
                               ;; Discard the curve if the points changed while
                               ;; we were working.
                               (cond-> new-val
                                 (= new-points old-points)
                                 (db/add-curve curve)))))
              (recur)))
          (catch Exception e
            (.printStackTrace e))))
      (Thread/sleep 250))
    stop?))

(defmethod integrant/halt-key! :engine
  [_ stop?]
  (reset! stop? true))

(defmethod integrant/init-key :state
  [_ props]
  (atom (db/init)))

(defmethod integrant/init-key :sketch
  [_ {:keys [anti-aliasing pixel-width pixel-height state x-point-min x-point-max y-point-min y-point-max]}]
  (let [x-scale (scales/linear [0 pixel-width] [x-point-min x-point-max])
        y-scale (scales/linear [pixel-height 0] [y-point-min y-point-max])]
    (applet/applet :size [pixel-width pixel-height]
                   :draw (fn [_] (core/draw! @state x-scale y-scale pixel-width))
                   :mouse-pressed (fn [_ event] (swap! state #(resampling/mouse-pressed % x-scale y-scale event)))
                   :key-typed (fn [_ event] (swap! state #(resampling/key-typed % event)))
                   ;; Why :no-bind-output is necessary: https://github.com/quil/quil/issues/216
                   :features [:keep-on-top :no-bind-output]
                   ;; Maybe we don't need this any more?
                   :middleware [#'middleware/fun-mode]
                   :settings #(quil/smooth anti-aliasing))))

(defmethod integrant/halt-key! :sketch
  [_ sketch]
  (.exit sketch))
