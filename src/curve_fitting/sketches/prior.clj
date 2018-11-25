(ns curve-fitting.sketches.prior
  (:require [metaprob.interpreters :as interpreters]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn sample-curve
  [points]
  (let [xs (map first points)
        ys (map second points)]
    (let [[_ trace score]
          (interpreters/infer :procedure model/curve-model
                              :inputs [xs]
                              :target-trace (trace/target-trace ys))]
      {:f (trace/coefficient-function (trace/coefficients trace))
       :score score})))
