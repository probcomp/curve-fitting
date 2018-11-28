(ns curve-fitting.inference
  (:refer-clojure :only [second])
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
   [metaprob.inference :refer :all :exclude [importance-resampling]]
   [metaprob.compositional :as comp]
   [metaprob.examples.gaussian :refer :all]))

(define importance-resampling
  (gen [model-procedure inputs target-trace intervention-trace N]
    ;; generate N candidate traces, called particles, each
    ;; with a score
    (define particles
    	(replicate N
	               (gen []
                   (define candidate-trace (mutable-trace))
                   (define [_ _ score]
                     (infer :procedure model-procedure
                            :inputs inputs
                            :intervention-trace intervention-trace
                            :target-trace target-trace
                            :output-trace candidate-trace))
                   [candidate-trace score])))
    (define scores
      (map (gen [p] (nth p 1)) particles))
    ;; return a trace with probability proportional to (exp score)
    (define which (log-categorical scores))

    (nth particles which)))
