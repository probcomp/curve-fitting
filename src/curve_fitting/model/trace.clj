(ns curve-fitting.model.trace
  "Functions for manipulating traces from the model."
  (:require [metaprob.builtin :as metaprob]))

;; Utility

(defn get-subtrace-in
  [trace path]
  (if-not (seq path)
    trace
    (let [trace-key (first path)]
      (if-not (metaprob/trace-has-subtrace? trace (list trace-key))
        nil
        (get-subtrace-in (metaprob/trace-subtrace trace trace-key)
                         (rest path))))))

(def points-subtrace-path '("map"))
(def point-y-path '(1 "gaussian")) ; path from point subtrace to y choice
(def point-outlier-path '(0 "outlier-point?" "flip")) ; path from point subtrace to outlier choice
(def outliers-enabled-path '(1 "add-noise-to-curve" 0 "outliers-enabled?" "flip")) ; path from root to outliers-enabled? choice

;; Cosntructors

(defn point-subtrace
  [{:keys [y outlier?]}]
  (cond-> (metaprob/empty-trace)
    (some? y) (metaprob/trace-set point-y-path y)
    (some? outlier?) (metaprob/trace-set point-outlier-path outlier?)))

(defn fix-points
  [trace points]
  (reduce (fn [trace [i point]]
            (metaprob/trace-set-subtrace trace
                                         (concat points-subtrace-path (list i))
                                         (point-subtrace point)))
          trace
          (zipmap (range (count points))
                  points)))

(defn points-trace
  [ys]
  (fix-points (metaprob/empty-trace) ys))

#_
(defn add-outlier-target-trace
  "Given a list of indexes, add an arm to the provided trace structure
  to fix the points at those indexes outliers or inliers"
  [traces mode i]
  (let [tf (if (= mode :inlier) false true)]
    (assoc-in traces ["map" i 2 "predicate" "outlier?" "then" "flip"] {:value tf})))

;; Points accessors

(defn point-count
  "Returns the number of points in `trace`."
  [trace]
  (if-let [points-subtrace (get-subtrace-in trace points-subtrace-path)]
    (count (metaprob/trace-keys points-subtrace))
    0))

(defn point-subtraces
  "Returns a sequence of the point subtraces in `trace`."
  [trace]
  (map #(metaprob/trace-subtrace trace (list "map" %))
       (range (point-count trace))))

(defn point-y
  "Returns the generated `y` from the point subtrace `subtrace`."
  [subtrace]
  (when (metaprob/trace-has? subtrace point-y-path)
    (metaprob/trace-get subtrace point-y-path)))

(defn point-outlier?
  "Returns the outlier choice for the point subtrace `subtrace`."
  [subtrace]
  (when (metaprob/trace-has? subtrace point-outlier-path)
    (metaprob/trace-get subtrace point-outlier-path)))

(defn point
  "Returns the point subtrace `subtrace` as a map."
  [subtrace]
  (let [y (point-y subtrace)
        outlier? (point-outlier? subtrace)]
    (cond-> {}
      (some? y) (assoc :y y)
      (some? outlier?) (assoc :outlier? outlier?))))

(defn points
  "Returns the points in `trace` as maps."
  [trace]
  (map point (point-subtraces trace)))

(defn outliers
  "Returns each trace's notion of 'outlierness' of each point"
  [trace]
  (map point-outlier? (point-subtraces trace)))

(defn outliers-enabled?
  "Returns true if outliers are enabled in `trace`."
  [trace]
  (metaprob/trace-get
   (metaprob/trace-subtrace trace outliers-enabled-path)))

;; Polynomial

(defn degree
  "Returns the degree from a trace of the model."
  [trace]
  (metaprob/trace-get
   (metaprob/trace-subtrace
    trace
    '(1 1 "generate-curve" 0 "degree" "uniform-sample"))))

(defn coefficients
  "Returns the coefficients from `trace` in order."
  [trace]
  (let [subtrace (get-subtrace-in trace [1 1 "generate-curve" 1 "coeffs" "replicate" "map"])]
    (map #(metaprob/trace-get subtrace (list % "f" "gaussian"))
         (range (degree trace)))))

(defn coefficient-function
  [coefficients]
  (fn [x]
    (->> coefficients
         (map-indexed (fn [i coefficient]
                        (* coefficient (Math/pow x i))))
         (reduce +))))

#_(let [[_ trace _] (metaprob.interpreters/infer :procedure curve-fitting.model/curve-model
                                                 :inputs [[1 2 3]]
                                                 :target-trace (target-trace [1 2 3]))]
    (outliers trace)
    [trace (coefficients trace)])
