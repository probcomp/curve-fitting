(ns curve-fitting.model
  (:refer-clojure :only [reduce])
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
    ;; Outliers are disabled by default. Enable with an intervention trace.
    (define outliers-enabled? (flip 1)) ; Are outliers enabled?
    (gen [x]
      (define outlier-point? (flip 0.1))
      (gaussian (curve x)
                (if (and outliers-enabled?
                         outlier-point?)
                  400
                  0.1)))))

(define curve-model
  (gen [xs]
    (map (add-noise-to-curve (generate-curve)) xs)))
