(ns curve-fitting.db
  "Functions for initializing and manipulating the application state.")

(defn init
  "Returns the initial state for the sketch."
  []
  {:mode       :prior
   :points     []
   :curves     []
   :digits     []
   :max-curves 20
   :outliers?  true
   :point-set  0})

(defn add-curve
  "Adds a curve to the sketch state."
  [state curve]
  (update state :curves conj curve))

(defn clear-curves
  "Removes all the curves from the sketch state."
  [state]
  (assoc state :curves []))

(defn add-point
  "Adds a point to the sketch state."
  [state point]
  (-> state
      (update :points conj point)
      (clear-curves)))

(defn set-points
  "Adds a point to the sketch state."
  [state [points point-set]]
  (-> state
      (assoc :points points)
      (assoc :point-set point-set)
      (clear-curves)))

(defn cycle-point-outlier-mode
  "Cycles a point through its states (model-generated outlier value,
  manually set inlier, manually set outlier, deleted)"
  [{:keys [points] :as state}]
  (assoc
   state
   :points
   (reduce (fn [ps p]
             (let [[selected current] ((juxt :selected :outlier-mode) p)]
               (if selected
                 (case current
                   :auto    (conj ps (assoc p :outlier-mode :inlier))
                   :inlier  (conj ps (assoc p :outlier-mode :outlier))
                   :outlier ps)
                 (conj ps p))))
           []
           (:points state))))

(defn mouse-pos
  "Notes the current mouse position."
  [state point]
  (-> state
      (assoc :mouse-pos point)))

(defn reset
  "Resets the sketch state to its initial value, clearing all points and curves."
  [state]
  (reset! state (init)))

(defn add-digit
  "Add a digit to the list of digits."
  [state raw-key]
  (update state :digits conj raw-key))

(defn delete-digit
  "Deletes the last digit from the list of digits."
  [state]
  (update state :digits (comp vec butlast)))

(defn- clear-digits
  "Clear the list of numbers."
  [state]
  (assoc state :digits []))

(defn set-max-curves
  "Parse the list of digits entered into a number and set it as the maximum number
  of curves."
  [{:keys [digits] :as state}]
  (cond-> state
    true (clear-digits)
    true (clear-curves)
    (seq digits) (assoc :max-curves (Integer/parseInt (apply str digits)))))

(defn toggle-mode
  "Sets the simulation to sample from the approximate posterior if it is sampling
  from the prior and vice versa."
  [{:keys [mode] :as state}]
  (-> state
      (clear-curves)
      (assoc :mode (get {:prior :resampling, :resampling :prior} mode))))

(defn outliers?
  "Returns true if outliers are enabled, false otherwise."
  [db]
  (:outliers? db))

(defn toggle-outliers
  "Toggles outliers on/off."
  [db]
  (-> db
      (clear-curves)
      (update :outliers? not)))
