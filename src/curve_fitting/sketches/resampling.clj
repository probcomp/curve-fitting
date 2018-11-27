(ns curve-fitting.sketches.resampling
  (:require [curve-fitting.db :as db]
            [curve-fitting.inference :as inference]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn sample-curve
  [points num-particles]
  (let [xs (map :x points)
        ys (map :y points)
        point-modes (into {} (map-indexed
                              (fn [i p] {i (:outlier-mode p)})
                              points))

        outlier-traces (reduce
                        (fn [traces [ix mode]]
                          (merge traces (case mode
                                          :inlier (trace/outlier-target-trace
                                                   :inlier ix)
                                          :outlier (trace/outlier-target-trace
                                                    :outlier ix)
                                          {})))
                        {}
                        (seq point-modes))

        [trace score] (inference/importance-resampling
                       model/curve-model
                       [xs]
                       (merge
                        outlier-traces
                        (trace/target-trace ys))
                       num-particles)]
    {:trace trace
     :score (Math/exp score)}))

(defn make-opacity-scale
  "Returns `255` regardless of input. All curves are at full opacity."
  [scores]
  (constantly 255))
