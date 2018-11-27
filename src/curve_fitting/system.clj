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
   :mode {:mode :prior}
   :sketch {:mode (integrant/ref :mode)
            :state (integrant/ref :state)
            :sketch-type :resampling

            :x-point-min -10
            :x-point-max 10

            :pixel-width 500
            :pixel-height 500

            :y-point-min -10
            :y-point-max 10

            :anti-aliasing 8}
   :engine {:mode (integrant/ref :mode)
            :state (integrant/ref :state)
            :sketch-type :resampling
            :num-particles 150}})

(defmethod integrant/init-key :mode
  [_ {:keys [mode]}]
  mode)

(defmethod integrant/init-key :engine
  [_ {:keys [mode state num-particles]}]
  (let [stop? (atom false)]
    (dotimes [_ 4]
      (sketches/sampling-thread stop? state (case mode
                                              :resampling #(resampling/sample-curve % num-particles)
                                              :prior      #(prior/sample-curve %)))
      ;; Offset starting the threads so curves don't arrive in bursts.
      (Thread/sleep 250))
    stop?))

(defmethod integrant/halt-key! :engine
  [_ stop?]
  (reset! stop? true))

(defmethod integrant/init-key :state
  [_ props]
  (atom (db/init)))

(defmethod integrant/init-key :sketch
  [_ {:keys [state mode pixel-width pixel-height x-point-min x-point-max y-point-min y-point-max num-particles]
      :as opts}]
  (let [x-scale (scales/linear [0 pixel-width] [x-point-min x-point-max])
        y-scale (scales/linear [pixel-height 0] [y-point-min y-point-max])
        make-opacity-scale (case mode
                             :resampling resampling/make-opacity-scale
                             :prior prior/make-opacity-scale)]
    (sketches/applet (merge (select-keys opts [:anti-aliasing :pixel-width :pixel-height])
                            {:mode mode
                             :state state
                             :x-scale x-scale
                             :y-scale y-scale
                             :make-opacity-scale make-opacity-scale}))))

(defmethod integrant/halt-key! :sketch
  [_ sketch]
  (.exit sketch))
