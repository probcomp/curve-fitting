(ns curve-fitting.model.distributions
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
   [incanter.distributions :as distributions]))

(define gamma
  (make-inference-procedure-from-sampler-and-scorer
   "gamma"
   (gen [shape scale]
     (distributions/draw
      (distributions/gamma-distribution shape scale)))
   (gen [x [shape scale]]
     (Math/log (distributions/pdf
                (distributions/gamma-distribution shape scale)
                x)))))

(define beta
  (make-inference-procedure-from-sampler-and-scorer
   "beta"
   (gen [alpha beta]
     (distributions/draw
      (distributions/beta-distribution alpha beta)))
   (gen [x [alpha beta]]
     (Math/log (distributions/pdf
                (distributions/beta-distribution alpha beta)
                x)))))
