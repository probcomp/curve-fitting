(ns curve-fitting.model.trace
  "Functions for manipulating traces from the model."
  (:require [metaprob.builtin :as metaprob]))

(defn target-trace
  "Returns a target trace that fixes the choices for `y` values in the model to
  the provided values."
  [ys]
  {"map"
   (into {} (map-indexed (fn [i y]
                           {i {"gaussian" {:value y}}})
                         ys))})

(defn degree
  "Returns the degree from a trace of the model."
  [trace]
  (metaprob/trace-get
   (metaprob/trace-subtrace
    trace
    '(1 1 "generate-curve" 0 "degree" "uniform-sample"))))

(defn coefficients
  "Returns the coefficients from a trace of the model."
  [trace]
  (let [coeff-subtraces (metaprob/trace-subtrace
                         trace
                         '(1 1 "generate-curve" 1 "coeffs" "replicate" "map"))]
    (map (fn [i]
           (metaprob/trace-get
            (metaprob/trace-subtrace
             coeff-subtraces
             (list i "f" "gaussian"))))
         (range (count (metaprob/trace-keys coeff-subtraces))))))

(defn coefficient-function
  [coefficients]
  (fn [x]
    (->> coefficients
         (map-indexed (fn [i coefficient]
                        (* coefficient (Math/pow x i))))
         (reduce +))))
