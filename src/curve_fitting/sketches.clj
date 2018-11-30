(ns curve-fitting.sketches
  (:require [quil.core :as quil]
            [quil.applet :as applet]
            [quil.middleware :as middleware]
            [curve-fitting.core :as core]
            [curve-fitting.db :as db]
            [curve-fitting.point-sets :as point-sets]
            [curve-fitting.scales :as scales]
            [curve-fitting.sketches.prior :as prior]
            [curve-fitting.sketches.resampling :as resampling]))

(defn mouse-pressed
  [state px-pt-scales event]
  (let [px-pt-x (:x px-pt-scales)
        px-pt-y (:y px-pt-scales)
        selected (filter #(:selected %) (:points state))
        new-state (if (seq selected)
                    (db/cycle-point-outlier-mode state)
                    (db/add-point state {:x (float (px-pt-x (:x event)))
                                         :y (float (px-pt-y (:y event)))
                                         :selected false
                                         :outlier-mode :auto}))]
    (db/clear-curves new-state)))

(defn mouse-moved
  [state px-pt-scales event]
  (db/update-selected state event px-pt-scales))

(defn key-typed
  [state px-pt-scales {:keys [raw-key] :as event}]
  (cond (= raw-key \c)
        (db/init)

        (= raw-key \t)
        (db/toggle-mode state)

        (= raw-key \o)
        (db/toggle-outliers state)

        (contains? #{\0 \1 \2 \3 \4 \5 \6 \7 \8 \9} raw-key)
        (db/add-digit state raw-key)

        (= raw-key \backspace)
        (db/delete-digit state)

        (= raw-key \newline)
        (db/set-max-curves state)

        (= raw-key \p)
        (db/set-points state (point-sets/next-point-set state px-pt-scales))

        :else (db/clear-curves state)))

(defn applet
  [{:keys [state px-pt-scales anti-aliasing]}]
  (let [pixel-width  (scales/domain-size (:x px-pt-scales))
        pixel-height (scales/domain-size (:y px-pt-scales))]
    (applet/applet :size [pixel-width pixel-height]
                   :draw (fn [_] (core/draw! @state px-pt-scales))
                   :mouse-pressed (fn [_ event] (swap! state #(mouse-pressed % px-pt-scales event)))
                   :mouse-moved   (fn [_ event] (swap! state #(mouse-moved   % px-pt-scales event)))
                   :key-typed     (fn [_ event] (swap! state #(key-typed     % px-pt-scales event)))
                   ;; Why :no-bind-output is necessary: https://github.com/quil/quil/issues/216
                   :features [:keep-on-top :no-bind-output]
                   ;; Maybe we don't need this any more?
                   :middleware [#'middleware/fun-mode]
                   :settings #(quil/smooth anti-aliasing))))

(defn sampling-thread
  [stop? state num-particles]
  (future
    (try
      (loop []
        (when-not @stop?
          (let [{old-points :points, outliers? :outliers?, :as old-state} @state
                curve (case (:mode old-state)
                        :resampling (resampling/sample-curve old-points outliers? num-particles)
                        :prior      (prior/sample-curve old-points outliers?))]
            (swap! state (fn [{:keys [curves max-curves] :as new-state}]
                           ;; Discard the curve if the points changed while
                           ;; we were working.
                           (cond-> new-state
                             (and (= (select-keys old-state [:points :mode :outliers?])
                                     (select-keys new-state [:points :mode :outliers?]))
                                  (< (count curves) max-curves))
                             (db/add-curve curve)))))
          (recur)))
      (catch Exception e
        (.printStackTrace e)))))
