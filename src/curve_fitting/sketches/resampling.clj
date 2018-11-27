(ns curve-fitting.sketches.resampling
  (:require [curve-fitting.db :as db]
            [curve-fitting.inference :as inference]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn sample-curve
  [points num-particles]
  (let [xs (map :x points)
        ys (map :y points)
        y-traces    (trace/target-trace ys)
        traces      (reduce
                     (fn [traces [ix mode]]
                       (case mode
                         :inlier (trace/add-outlier-target-trace
                                  traces :inlier ix)
                         :outlier (trace/add-outlier-target-trace
                                   traces :outlier ix)
                         traces))
                     y-traces
                     (map-indexed (fn [i p] [i (:outlier-mode p)])
                                  points))
        [trace score] (inference/importance-resampling
                       model/curve-model
                       [xs]
                       traces
                       num-particles)]
    {:trace trace
     :score (Math/exp score)}))

(defn make-opacity-scale
  "Returns `255` regardless of input. All curves are at full opacity."
  [scores]
  (constantly 255))
