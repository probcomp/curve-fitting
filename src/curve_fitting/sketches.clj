(ns curve-fitting.sketches
  (:require [quil.core :as quil]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.core :as core]
            [curve-fitting.db :as db]))

(defn mouse-pressed
  [state x-scale y-scale event]
  (let [{:keys [x y]} event]
    ;; (db/add-point state [(x-scale x) (y-scale y)])
    (db/add-point state {:x (x-scale x)
                         :y (y-scale y)
                         :selected false})))

(defn mouse-moved
  [state x-scale y-scale event]
  (let [{:keys [x y]} event
        updated-state (db/mouse-pos state
                                    [(x-scale x) (y-scale y)])]

    (if (seq? (:points updated-state))
      (doseq [[ix point] (map-indexed vector (:points updated-state))]
        (let [selection-threshold 10
              pixel-x (x-scale (:x point))
              pixel-y (y-scale (:y point))
              mp-x    (x-scale x)
              mp-y    (y-scale y)
              distance (Math/sqrt (+ (Math/pow (- pixel-x mp-x) 2)
                                     (Math/pow (- pixel-y mp-y) 2)))]
          (if (< distance selection-threshold)
            (db/select-point updated-state ix)
            (db/deselect-point updated-state ix))))
      updated-state)))

(defn key-typed
  [state {:keys [raw-key]}]
  (if (contains? #{\backspace} raw-key)
    (db/init)
    (db/clear-curves state)))

(defn applet
  [{:keys [anti-aliasing state pixel-width pixel-height x-scale y-scale make-opacity-scale]}]
  (applet/applet :size [pixel-width pixel-height]
                 :draw (fn [_] (core/draw! @state x-scale y-scale pixel-width pixel-height make-opacity-scale))
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
            (swap! state (fn [{new-points :points :as new-val}]
                           ;; Discard the curve if the points changed while
                           ;; we were working.
                           (cond-> new-val
                             (= new-points old-points)
                             (db/add-curve curve)))))
          (recur)))
      (catch Exception e
        (.printStackTrace e)))))
