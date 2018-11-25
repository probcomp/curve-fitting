(ns curve-fitting.db
  "Functions for initializing and manipulating the application state.")

(defn init
  "Returns the initial state for the sketch."
  []
  {:points [], :curves []})

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
