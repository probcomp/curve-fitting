(ns curve-fitting.system
  (:require [integrant.core :as integrant]
            [quil.core :as quil]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.draw :as draw]
            [curve-fitting.db :as db]
            [curve-fitting.scales :as scales]
            [curve-fitting.sketches :as sketches]
            [curve-fitting.sketches.prior :as prior]
            [curve-fitting.sketches.resampling :as resampling]))

(def config
  {:state {}
   :sketch {:state (integrant/ref :state)

            :dimensions {:pixel {:x {:min 0, :max 500}
                                 :y {:min 500, :max 0}}
                         :point {:x {:min -5, :max 5}
                                 :y {:min -10, :max 10}}}

            :anti-aliasing 8}
   :engine {:state (integrant/ref :state)
            :num-particles 1000}})

(defmethod integrant/init-key :mode
  [_ {:keys [mode]}]
  mode)

(defmethod integrant/init-key :engine
  [_ {:keys [mode state num-particles]}]
  (let [stop? (atom false)]
    (dotimes [_ 4]
      (sketches/sampling-thread stop? state num-particles)
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
  [_ {:keys [state dimensions anti-aliasing] :as opts}]
  (let [x-px-pt (scales/linear [(get-in dimensions [:pixel :x :min])
                                (get-in dimensions [:pixel :x :max])]
                               [(get-in dimensions [:point :x :min])
                                (get-in dimensions [:point :x :max])])

        y-px-pt (scales/linear [(get-in dimensions [:pixel :y :min])
                                (get-in dimensions [:pixel :y :max])]
                               [(get-in dimensions [:point :y :min])
                                (get-in dimensions [:point :y :max])])]


    (def ysc y-px-pt)
    (sketches/applet {:state state
                      :px-pt-scales {:x x-px-pt, :y y-px-pt}
                      :anti-aliasing anti-aliasing})))

(defmethod integrant/halt-key! :sketch
  [_ sketch]
  (.exit sketch))
