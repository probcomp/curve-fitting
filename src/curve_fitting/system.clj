(ns curve-fitting.system
  (:require [integrant.core :as integrant]
            [quil.core :as quil]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.core :as core]
            [curve-fitting.db :as db]
            [curve-fitting.scales :as scales]
            [curve-fitting.sketches :as sketches]
            [curve-fitting.sketches.prior :as prior]
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
            :sketch-type :prior
            :num-particles 150}})

(defmethod integrant/init-key :engine
  [_ {:keys [state sketch-type num-particles]}]
  (let [stop? (atom false)]
    (dotimes [_ 4]
      (sketches/sampling-thread stop? state (case sketch-type
                                              :resampling #(resampling/sample-curve % num-particles)
                                              :prior      #(prior/sample-curve %)))
      (Thread/sleep 250))
    stop?))

(defmethod integrant/halt-key! :engine
  [_ stop?]
  (reset! stop? true))

(defmethod integrant/init-key :state
  [_ props]
  (atom (db/init)))

(defmethod integrant/init-key :sketch
  [_ {:keys [state pixel-width pixel-height x-point-min x-point-max y-point-min y-point-max num-particles]
      :as opts}]
  (let [x-scale (scales/linear [0 pixel-width] [x-point-min x-point-max])
        y-scale (scales/linear [pixel-height 0] [y-point-min y-point-max])]
    (sketches/applet (merge (select-keys opts [:anti-aliasing :pixel-width :pixel-height])
                            {:state state
                             :x-scale x-scale
                             :y-scale y-scale}))))

(defmethod integrant/halt-key! :sketch
  [_ sketch]
  (.exit sketch))
