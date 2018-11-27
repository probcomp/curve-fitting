(ns curve-fitting.sketches
  (:require [quil.core :as quil]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.core :as core]
            [curve-fitting.db :as db]
            [curve-fitting.scales :as scales]))

(defn mouse-pressed
  [state x-scale y-scale event]
  (let [{:keys [x y]} event
        selected (filter #(:selected %) (:points state))
        new-state (if (seq selected)
                    (db/cycle-point-outlier-mode state)
                    (db/add-point state {:x (x-scale x)
                                         :y (y-scale y)
                                         :selected false
                                         :outlier-mode :auto}))]
    (db/clear-curves new-state)))

(defn mouse-moved
  [state x-scale y-scale event]
  (let [{:keys [x y]} event
        updated-state (db/mouse-pos state
                                    [(x-scale x) (y-scale y)])]
    (assoc
     updated-state
     :points
     (vec (map (fn [point]
                 (let [selection-threshold 0.2
                       m-x    (x-scale x)
                       m-y    (y-scale y)
                       p-x    (:x point)
                       p-y    (:y point)
                       distance (Math/sqrt
                                 (+ (Math/pow (- p-x m-x) 2)
                                    (Math/pow (- p-y m-y) 2)))]
                   (if (< distance selection-threshold)
                     (assoc point :selected true)
                     (assoc point :selected false))))
               (:points updated-state))))))

(defn key-typed
  [state {:keys [raw-key] :as event}]
  (cond (= raw-key \c)
        (db/init)

        (contains? #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9} raw-key)
        (db/add-digit state raw-key)

        (= raw-key \backspace)
        (db/delete-digit state)

        (= raw-key \newline)
        (db/set-max-curves state)

        :else (db/clear-curves state)))

(defn applet
  [{:keys [state mode pixel-width pixel-height x-scale y-scale anti-aliasing make-opacity-scale max-curves]}]
  (applet/applet :size [pixel-width pixel-height]
                 :draw (fn [_] (core/draw! @state mode x-scale y-scale pixel-width pixel-height make-opacity-scale))
                 :mouse-pressed (fn [_ event] (swap! state #(mouse-pressed % x-scale y-scale event)))
                 :mouse-moved (fn [_ event] (swap! state #(mouse-moved % x-scale y-scale event)))
                 :key-typed (fn [_ event] (swap! state #(key-typed % event)))
                 ;; Why :no-bind-output is necessary: https://github.com/quil/quil/issues/216
                 :features [:keep-on-top :no-bind-output]
                 ;; Maybe we don't need this any more?
                 :middleware [#'middleware/fun-mode]
                 :settings #(quil/smooth anti-aliasing)))

(defn sampling-thread
  [stop? state sample-curve]
  (future
    (try
      (loop []
        (when-not @stop?
          (let [{old-points :points :as old-val} @state
                curve (sample-curve old-points)]
            (swap! state (fn [{new-points :points, curves :curves, max-curves :max-curves :as new-val}]
                           ;; Discard the curve if the points changed while
                           ;; we were working.
                           (cond-> new-val
                             (and (= new-points old-points)
                                  (< (count curves) max-curves))
                             (db/add-curve curve)))))
          (recur)))
      (catch Exception e
        (.printStackTrace e)))))
