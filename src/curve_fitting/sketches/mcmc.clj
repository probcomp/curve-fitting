(ns curve-fitting.sketches.mcmc
  (:refer-clojure :exclude [update])
  (:require [metaprob.builtin :as metaprob]
            [metaprob.distributions :as distributions]
            [metaprob.examples.gaussian :as gaussian]
            [metaprob.interpreters :refer [infer]]
            [curve-fitting.db :as db]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]))
;
;(defn outlier-proposal
;  [trace]
;  (let [points (trace/points trace)
;        point-i (distributions/uniform-sample (range (count points)))]
;    (trace/update-outlier trace point-i not)))
;
;(defn coefficient-proposal
;  [trace]
;  (let [degree (trace/degree trace)
;        coefficient-i (distributions/uniform-sample (range degree))]
;    (trace/update-coefficient trace coefficient-i #(gaussian/gaussian % 0.05))))
;
;(defn proposal
;  [trace]
;  (if-not (seq (trace/points trace))
;    (coefficient-proposal trace)
;    (case (distributions/categorical [0.5 0.5])
;      0 (coefficient-proposal trace)
;      1 (outlier-proposal trace))))
;
;(defn accept?
;  [trace proposal {:keys [xs outliers?] :as opts}]
;  (let [intervention-trace (trace/outliers-trace outliers?)
;        [_ _ current-score] (infer :procedure model/curve-model
;                                   :inputs [xs]
;                                   :target-trace trace
;                                   :intervention-trace intervention-trace)
;        [_ _ new-score] (infer :procedure model/curve-model
;                               :inputs [xs]
;                               :target-trace proposal
;                               :intervention-trace (trace/outliers-trace outliers?))
;        current-prob (Math/exp current-score)
;        new-prob (Math/exp new-score)]
;    (> new-prob current-prob)))
;
;(defn update-trace
;  [trace state]
;  (let [points (db/points state)
;        xs (map :x points)
;        outliers? (db/outliers? state)
;        proposal (proposal trace)]
;    (if (accept? trace proposal {:xs xs, :outliers outliers?})
;      proposal
;      trace)))

(defn curve-addresses
  [trace]
  (filter
    (fn [addr] (or (= addr trace/degree-path) (= trace/coefficients-subtrace-path (take (count trace/coefficients-subtrace-path) addr))))
    (metaprob/addresses-of trace)))

(defn next-trace
  [trace xs]
  (let
    [resim-move
     (fn [tr resim-addrs]
       (let
         [[current-choices other-choices] (trace/partition-trace tr resim-addrs)
          [_ _ current-score] (infer :procedure model/curve-model :inputs [xs] :target-trace other-choices :intervention-trace current-choices)
          [_ proposed new-score] (infer :procedure model/curve-model :inputs [xs] :target-trace other-choices)]
         (if (< (Math/log (distributions/uniform 0 1)) (- new-score current-score)) proposed tr)))

     resim-curve
     (fn [tr]
       (resim-move tr (curve-addresses tr)))

     gibbs-outliers-sweep
     (fn [tr]
       (let
         [pts (trace/points tr)
          f   (trace/coefficient-function (trace/coefficients tr))
          inlier-noise (trace/inlier-noise tr)
          outlier-noise (trace/outlier-noise tr)
          outlier-prob (trace/prob-outlier tr)
          new-pts
          (map
            (fn [x p]
              (let [y (p :y)
                    inlier-score (+ (Math/log (- 1 outlier-prob))
                                    (gaussian/score-gaussian y [(f x) inlier-noise]))
                    outlier-score (+ (Math/log outlier-prob)
                                     (gaussian/score-gaussian y [0 outlier-noise]))]
                (assoc p :outlier? (nth [false true] (distributions/log-categorical `(~inlier-score ~outlier-score))))))
             xs pts)]
         (trace/fix-points tr new-pts)))

     resim-hypers
     (fn [tr]
       (resim-move tr trace/hyperparameter-paths))

     walk-coeffs
     (fn [tr]
       (reduce
         (fn [tr i]
           (let
             [[_ _ current-score] (infer :procedure model/curve-model :target-trace tr :inputs [xs])
              width 0.1
              proposal (trace/update-coefficient tr i (fn [c] (+ c (* width (gaussian/gaussian 0 1)))))
              [_ _ new-score] (infer :procedure model/curve-model :target-trace proposal :inputs [xs])]
             (if (< (Math/log (distributions/uniform 0 1)) (- new-score current-score))
                 proposal
                 tr)))
         tr
         (range (trace/degree tr))))]

  (walk-coeffs (resim-hypers (gibbs-outliers-sweep (resim-curve trace))))))

(defn update-trace
  [trace state]
  (let
    [xs (map :x (db/points state))]
    (next-trace trace xs)))

(defn- update-curve
  [curve state]
  (clojure.core/update curve :trace #(update-trace % state)))

(defn- update-curves
  [curves state]
  (map #(update-curve % state) curves))

(defn update
  [state]
  (clojure.core/update state :curves #(update-curves % state)))
