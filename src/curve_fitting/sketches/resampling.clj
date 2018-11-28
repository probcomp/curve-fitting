(ns curve-fitting.sketches.resampling
  (:require [curve-fitting.db :as db]
            [curve-fitting.inference :as inference]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn sample-curve
  [points outliers? num-particles]
  (def outliers? outliers?)
  (let [xs (map :x points)
        [trace score] (inference/importance-resampling
                       model/curve-model
                       [xs]
                       (trace/points-trace points)
                       (trace/outliers-trace outliers?)
                       num-particles)]
    (def trace trace)
    {:trace trace
     :score (Math/exp score)}))

(defn make-opacity-scale
  "Returns `255` regardless of input. All curves are at full opacity."
  [scores]
  (constantly 255))
