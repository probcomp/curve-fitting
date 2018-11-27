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
