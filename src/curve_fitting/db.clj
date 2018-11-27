(ns curve-fitting.db
  "Functions for initializing and manipulating the application state.")

(defn init
  "Returns the initial state for the sketch."
  []
  {:points     []
   :curves     []
   :numbers    []
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

(defn add-number
  "Add a number to the list of numbers."
  [state raw-key]
  (update state :numbers conj raw-key))

(defn- clear-numbers
  "Clear the list of numbers."
  [state]
  (assoc state :numbers []))

(defn set-max-curves
  "Parse the list of numbers entered into an integer and set it as the number of max curves."
  [{:keys [numbers] :as state}]
  (cond-> state
    true (clear-numbers)
    true (clear-curves)
    (seq numbers) (assoc :max-curves (Integer/parseInt (apply str numbers)))))
