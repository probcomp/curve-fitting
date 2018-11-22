(ns curve-fitting.model
  (:refer-clojure :only [let reduce])
  (:require
   [metaprob.state :as state]
   [metaprob.trace :as trace]
   [metaprob.sequence :as sequence]
   [metaprob.builtin-impl :as impl]
   [metaprob.syntax :refer :all]
   [metaprob.builtin :refer :all]
   [metaprob.prelude :refer :all]
   [metaprob.distributions :refer :all]
   [metaprob.interpreters :refer :all]
   [metaprob.inference :refer :all]
   [metaprob.compositional :as comp]
   [metaprob.examples.gaussian :refer :all]))

(define generate-curve
  (gen []
    (define degree
      (uniform-sample [1 2 3 4]))
    (define coeffs
      (replicate degree (gen [] (gaussian 0 1))))
    (gen [x]
      (reduce + (map (gen [n] (* (nth coeffs n) (expt x n)))
                     (range degree))))))

(define add-noise-to-curve
  (gen [curve]
    (gen [x] (gaussian (curve x) 0.1))))

(define curve-model
  (gen [xs]
    (map (add-noise-to-curve (generate-curve)) xs)))

(define get-coeffs-from-trace
  (gen [tr]
    (define degree
      (trace-get
       (trace-subtrace
        tr
        '(1 1 "generate-curve" 0 "degree" "uniform-sample"))))

    (define coeff-subtraces
      (trace-subtrace
       tr
       '(1 1 "generate-curve" 1 "coeffs" "replicate" "map")))

    (map (gen [k]
           (trace-get
            (trace-subtrace
             coeff-subtraces
             (list k "f" "gaussian"))))
         (trace-keys coeff-subtraces))))

#_(let [xs '(-0.5 -0.3 0.1 0.2 0.5)
        observations {"map"
                      {0 {"gaussian" {:value 0.06}}, 1 {"gaussian" {:value 0.36}},
                       2 {"gaussian" {:value 0.62}}, 3 {"gaussian" {:value 0.68}},
                       4 {"gaussian" {:value 1.03}}}}

        result (infer :procedure curve-model
                      :inputs [xs]
                      :target-trace observations)]
    (get-coeffs-from-trace (nth result 1)))
