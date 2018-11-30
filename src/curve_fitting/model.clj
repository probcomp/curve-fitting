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
   [metaprob.examples.gaussian :refer :all]
   [curve-fitting.model.distributions :refer :all]))

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
    (define outliers-enabled? (flip 1))

    ;; Hyperparameters
    (define inlier-noise (gamma 2 1))
    (define outlier-noise (gamma 10 1))
    (define prob-outlier (beta 1 20))

    ;; Outliers are enabled by default. Enable with an intervention trace.
    (gen [x]
      (define outlier-point? (flip prob-outlier))
      (gaussian (curve x)
                (if (and outliers-enabled?
                         outlier-point?)
                  outlier-noise
                  inlier-noise)))))

(define curve-model
  (gen [xs]
    (map (add-noise-to-curve (generate-curve)) xs)))
