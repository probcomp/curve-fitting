(ns curve-fitting.scales)

(defprotocol Size
  (domain-size [this] "Returns with width of the domain.")
  (range-size  [this] "Returns the width of the range."))

(defrecord LinearScale [domain-min domain-max range-min range-max]
  Size
  (domain-size [{:keys [domain-min domain-max]}]
    (Math/abs (- domain-max domain-min)))
  (range-size [{:keys [range-min range-max]}]
    (Math/abs (- range-max range-min)))

  clojure.lang.IFn
  (invoke [{:keys [domain-min domain-max range-min range-max] :as scale} n]
    (if (= domain-min domain-max)
      range-max
      (let [domain-span (- domain-max domain-min)
            range-span (- range-max range-min)]
        (+ range-min (* range-span
                        (/ (- n domain-min)
                           domain-span)))))))
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
