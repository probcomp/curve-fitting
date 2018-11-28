(ns curve-fitting.sketches.prior
  (:require [metaprob.interpreters :as interpreters]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]
            [curve-fitting.scales :as scales]))

(defn sample-curve
  [points outliers?]
  (let [xs (map :x points)
        ys (map :y points)]
    (let [[_ trace log-score]
          (interpreters/infer :procedure model/curve-model
                              :inputs [xs]
                              :target-trace (trace/points-trace points)
                              :intervention-trace (trace/outliers-trace outliers?))]
      {:trace trace
       :log-score log-score})))

(defn exp
  [x]
  (Math/exp x))

(defn make-opacity-scale
  [log-scores]
  (let [probabilities (map exp log-scores)
        sum (reduce + probabilities)]
    (if (zero? sum)
      (constantly 255)
      (let [rescaled (map #(/ % sum) probabilities)
            max-rescaled (apply max rescaled)
            min-rescaled (apply min rescaled)
            linear-scale (scales/linear [min-rescaled sum]
                                        [0 255])]
        (fn [score]
          (* 255 (/ (exp score)
                    sum)))))))
