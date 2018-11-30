(ns curve-fitting.sketches.mcmc
  (:refer-clojure :exclude [update])
  (:require [metaprob.builtin :as metaprob]
            [metaprob.distributions :as distributions]
            [metaprob.examples.gaussian :as gaussian]
            [metaprob.interpreters :refer [infer]]
            [curve-fitting.db :as db]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))

(defn outlier-proposal
  [trace]
  (let [points (trace/points trace)
        point-i (distributions/uniform-sample (range (count points)))]
    (trace/update-outlier trace point-i not)))

(defn coefficient-proposal
  [trace]
  (let [degree (trace/degree trace)
        coefficient-i (distributions/uniform-sample (range degree))]
    (trace/update-coefficient trace coefficient-i #(gaussian/gaussian % 0.05))))

(defn proposal
  [trace]
  (if-not (seq (trace/points trace))
    (coefficient-proposal trace)
    (case (distributions/categorical [0.5 0.5])
      0 (coefficient-proposal trace)
      1 (outlier-proposal trace))))

(defn accept?
  [trace proposal {:keys [xs outliers?] :as opts}]
  (let [intervention-trace (trace/outliers-trace outliers?)
        [_ _ current-score] (infer :procedure model/curve-model
                                   :inputs [xs]
                                   :target-trace trace
                                   :intervention-trace intervention-trace)
        [_ _ new-score] (infer :procedure model/curve-model
                               :inputs [xs]
                               :target-trace proposal
                               :intervention-trace (trace/outliers-trace outliers?))
        current-prob (Math/exp current-score)
        new-prob (Math/exp new-score)]
    (> new-prob current-prob)))

(defn update-trace
  [trace state]
  (let [points (db/points state)
        xs (map :x points)
        outliers? (db/outliers? state)
        proposal (proposal trace)]
    (if (accept? trace proposal {:xs xs, :outliers outliers?})
      proposal
      trace)))

(defn- update-curve
  [curve state]
  (clojure.core/update curve :trace #(update-trace % state)))

(defn- update-curves
  [curves state]
  (map #(update-curve % state) curves))

(defn update
  [state]
  (clojure.core/update state :curves #(update-curves % state)))
