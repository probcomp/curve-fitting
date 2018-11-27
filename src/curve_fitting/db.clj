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

(defn mouse-pos
  "Notes the current mouse position."
  [state point]
  (-> state
      (assoc :mouse-pos point)))

(defn select-point
  "Indicates the specified point is selected"
  [state ix]
  (-> state
      (assoc-in [:points ix :selected] true)))

(defn deselect-point
  "Indicates the specified point is selected"
  [state ix]
  (let [new-state (-> state
                      (assoc-in [:points ix :selected] false))]
    new-state))

(defn reset
  "Resets the sketch state to its initial value, clearing all points and curves."
  [state]
  (reset! state (init)))
