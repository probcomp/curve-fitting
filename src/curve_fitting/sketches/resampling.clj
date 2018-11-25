(ns curve-fitting.sketches.resampling
  (:require [curve-fitting.db :as db]
            [curve-fitting.inference :as inference]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn points-to-curve
  [num-particles points]
  (let [xs (map first points)
        ys (map second points)]
    (let [[trace score] (inference/importance-resampling
                         model/curve-model
                         [xs]
                         (trace/target-trace ys)
                         num-particles)]
      {:f (trace/coefficient-function (trace/coefficients trace))
       :score score})))

(defn mouse-pressed
  [state x-scale y-scale event]
  (let [{:keys [x y]} event]
    (db/add-point state [(x-scale x) (y-scale y)])))

(defn key-typed
  [state {:keys [raw-key]}]
  (if (contains? #{\backspace} raw-key)
    (db/init)
    (db/clear-curves state)))
