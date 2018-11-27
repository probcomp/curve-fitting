(ns curve-fitting.db
  "Functions for initializing and manipulating the application state.")

(defn init
  "Returns the initial state for the sketch."
  []
  {:mode       :prior
   :points     []
   :curves     []
   :digits     []
   :max-curves 20})

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
  [{:keys [mode] :as state}]
  (-> state
      (clear-curves)
      (assoc :mode (get {:prior :resampling, :resampling :prior} mode))))
