(ns curve-fitting.model.trace
  "Functions for manipulating traces from the model."
  (:require [metaprob.builtin :as metaprob]))

;; Utility

(defn trace-update
  "`clojure.core/update-in` but for traces."
  [trace path f]
  (let [v (metaprob/trace-get trace path)
        new-v (f v)]
    (metaprob/trace-set trace path new-v)))

(defn get-subtrace-in
  "`clojure.core/get-in`, but for traces. Retrieves the subtrace at the provided
  key path if it exists. Returns `nil` if any of the keys in the path are
  missing."
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
  "Generates a point subtrace that matches the values in `point`. `point` is a
  map with two optional keys: `:y` and `:outlier?`."
  [point]
  (let [{:keys [y outlier?]} point]
    (cond-> (metaprob/empty-trace)
      (some? y) (metaprob/trace-set point-y-path y)
      (some? outlier?) (metaprob/trace-set point-outlier-path outlier?))))

(defn fix-points
  "Modifies `trace` such that the choices the model makes concerning points match
  the values in `points`. See `point-subtrace` for a description of `points`."
  [trace points]
  (reduce (fn [trace [i point]]
            (metaprob/trace-set-subtrace trace
                                         (concat points-subtrace-path (list i))
                                         (point-subtrace point)))
          trace
          (zipmap (range (count points))
                  points)))

(defn update-outlier
  ""
  [trace i f]
  (trace-update trace
                (concat points-subtrace-path
                        '(0)
                        point-outlier-path)
                f))

(defn points-trace
  "Returns a trace that fixes the model's outputs to `ys`."
  [ys]
  (fix-points (metaprob/empty-trace) ys))

(defn outliers-trace
  "Returns a trace that fixes the choice of whether outliers are enabled to
  `outliers?`."
  [outliers?]
  (metaprob/trace-set (metaprob/empty-trace) outliers-enabled-path outliers?))

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

(defn outliers-enabled?
  "Returns true if outliers are enabled in `trace`."
  [trace]
  (metaprob/trace-get
   (metaprob/trace-subtrace trace outliers-enabled-path)))

(defn outliers
  "Returns each trace's notion of 'outlierness' of each point. Will always be
  false if outliers are disabled."
  [trace]
  (let [outliers-enabled? (outliers-enabled? trace)]
    (map (fn [point]
           (and outliers-enabled? (point-outlier? point)))
         (point-subtraces trace))))

;; Polynomial

(def degree-path '(1 1 "generate-curve" 0 "degree" "uniform-sample"))
(defn coefficient-subtrace-path [i] (list i "f" "gaussian"))
(def coefficients-subtrace-path '(1 1 "generate-curve" 1 "coeffs" "replicate" "map"))

(defn degree
  "Returns the degree from a trace of the model."
  [trace]
  (metaprob/trace-get (metaprob/trace-subtrace trace degree-path)))

(defn fix-degree
  "Fixes the degree in `trace` to `degree`."
  [trace degree]
  (metaprob/trace-set (metaprob/trace-subtrace trace) degree))

(defn update-coefficient
  "Returns the `i`th coefficient in `trace`."
  [trace i f]
  (let [coefficient-path (concat coefficients-subtrace-path
                                 (coefficient-subtrace-path i))]
    (trace-update trace coefficient-path f)))

(defn coefficients
  "Returns the coefficients from `trace` in order."
  [trace]
  (let [subtrace (get-subtrace-in trace coefficients-subtrace-path)]
    (map #(metaprob/trace-get subtrace (coefficient-subtrace-path %))
         (range (degree trace)))))

(defn coefficient-function
  "Takes a sequence of coefficients and creates a polynomial function from it."
  [coefficients]
  (fn [x]
    (->> coefficients
         (map-indexed (fn [i coefficient]
                        (* coefficient (Math/pow x i))))
         (reduce +))))

;; Hyperparameters

(defn hyperparameters-path [i] (list 1 "add-noise-to-curve" i))
(defn- hyperparameter-path [index name type] (concat (hyperparameters-path index) [name type]))

(defn inlier-noise
  [trace]
  (metaprob/trace-get trace (hyperparameter-path 1 "inlier-noise" "gamma")))

(defn outlier-noise
  [trace]
  (metaprob/trace-get trace (hyperparameter-path 2 "outlier-noise" "gamma")))

(defn prob-outlier
  [trace]
  (metaprob/trace-get trace (hyperparameter-path 3 "prob-outlier" "beta")))

(defn fix-inlier-noise
  [trace val]
  (metaprob/trace-set trace
                      (hyperparameter-path 1 "inlier-noise" "gamma")
                      val))

(defn fix-outlier-noise
  [trace val]
  (metaprob/trace-set trace
                      (hyperparameter-path 2 "outlier-noise" "gamma")
                      val))

(defn fix-prob-outlier
  [trace val]
  (metaprob/trace-set trace
                      (hyperparameter-path 3 "prob-outlier" "beta")
                      val))
