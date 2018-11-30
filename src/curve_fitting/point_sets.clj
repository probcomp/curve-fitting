(ns curve-fitting.point-sets
  (:require
   [metaprob.interpreters :as interpreters]
   [curve-fitting.model :as model]
   [curve-fitting.model.trace :as trace]
   [curve-fitting.scales :as scales]))

(def point-coeffs
  [[0 0.5]
   [0 1 0.5]])

(defn next-point-set
  [state px-pt-scales]

  (let [point-set-ix (:point-set state)
        {x-px-pt :x, y-px-pt :y} px-pt-scales
        indent   (* 0.05 (scales/range-size x-px-pt))
        indented-scale (scales/linear [0 9]
                                      [(+ (:range-min x-px-pt) indent)
                                       (- (:range-max x-px-pt) indent)])
        xs       (map indented-scale (range 0 10))
        coeffs   (point-coeffs point-set-ix)
        [ys tr]  (interpreters/infer :procedure model/curve-model
                                     :inputs [xs]
                                     :intervention-trace (trace/coefficients-trace coeffs))]

    ;; (clojure.pprint/pprint tr)

    [(map (fn [x y] {:x x :y y
                     :selected false :outlier-mode :auto})
          xs ys)
     (mod (inc point-set-ix) (count point-coeffs))]))
