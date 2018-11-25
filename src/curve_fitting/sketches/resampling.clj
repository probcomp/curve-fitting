(ns curve-fitting.sketches.resampling
  (:require [curve-fitting.db :as db]
            [curve-fitting.inference :as inference]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn sample-curve
  [points num-particles]
  (let [xs (map first points)
        ys (map second points)]
    (let [[trace score] (inference/importance-resampling
                         model/curve-model
                         [xs]
                         (trace/target-trace ys)
                         num-particles)]
      {:f (trace/coefficient-function (trace/coefficients trace))
       :score score})))
