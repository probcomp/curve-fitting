(ns curve-fitting.scales)

(defrecord LinearScale [domain-min domain-max range-min range-max]
  clojure.lang.IFn
  (invoke [{:keys [domain-min domain-max range-min range-max]} n]
    (let [domain-span (- domain-max domain-min)
          range-span (- range-max range-min)]
      (+ range-min (* range-span
                      (/ (float (- n domain-min))
                         domain-span))))))
(defn linear
  [domain range]
  (->LinearScale (first domain) (last domain) (first range) (last range)))

(defrecord QuantileScale [mapping]
  clojure.lang.IFn
  (invoke [{:keys [mapping]} n]
    (mapping n)))

(defn quantile
  [domain range]
  (let [num-partitions (quot (count domain)
                             (count range))]
    (->QuantileScale
     (into (sorted-map)
           (comp (partition-all num-partitions)
                 (map-indexed (fn [i domain-partition]
                                (for [domain-val domain-partition]
                                  {domain-val (nth range i (last range))})))
                 cat)
           domain))))

(defn invert
  [{:keys [domain-min domain-max range-min range-max]}]
  ;; Should be a protocol.
  (linear [range-min range-max] [domain-min domain-max]))
