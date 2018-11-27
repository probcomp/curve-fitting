(ns curve-fitting.core
  (:require [metaprob.builtin :as metaprob]
            [metaprob.interpreters :as interpreters]
            [quil.applet :as applet]
            [quil.core :as quil]
            [quil.middleware :as middleware]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]
            [curve-fitting.inference :as inference]
            [curve-fitting.scales :as scales]))

(def point-pixel-radius 8)
(def num-curves 10)

(defn draw-plot [f from to step inverted-x-scale inverted-y-scale]
  (quil/no-fill)
  (quil/begin-shape)
  (doseq [[x y] (->> (range from to step)
                     (map (fn [x]
                            (let [y (f x)]
                              [(inverted-x-scale x)
                               (inverted-y-scale y)]))))]
    (quil/curve-vertex x y))
  (quil/end-shape))

(defn draw-point-selection!
  [pixel-x pixel-y mouse-px mouse-py red-value blue-value]
  (let [active-radius 10
        circle-radius 10
        distance (Math/sqrt (+ (Math/pow (- pixel-x mouse-px) 2)
                               (Math/pow (- pixel-y mouse-py) 2)))]
    (when (< distance active-radius)
      (do
        (defn draw-circle []
          (quil/arc pixel-x
                    pixel-y
                    (* 2 circle-radius)
                    (* 2 circle-radius)
                    0
                    (* 2 3.141)))
        (quil/fill 0 0 0 0)
        (quil/stroke-weight 4)
        (quil/stroke 255 255 255 192)
        (draw-circle)
        (quil/stroke-weight 1)
        (quil/stroke red-value 0 blue-value 192)
        (draw-circle)))))

(defn draw-clicked-points!
  "Draws the given points onto the current sketch."
  [points curves [mouse-x mouse-y] inverted-x-scale inverted-y-scale]
  (quil/no-stroke)

  (let [trace-outliers (map trace/outliers (map :trace curves))
        outlier-scores (if (empty? trace-outliers)
                         (repeat (count points) 0)
                         (map #(/ (count (filter true? %))
                                  (count curves))
                              (apply map vector trace-outliers)))]
    (doseq [[[point-x point-y] outlier-score] (map list points
                                                   outlier-scores)]
      (let [red-value  (int (* 255 outlier-score))
            blue-value (int (- 255 red-value))
            pixel-x (inverted-x-scale point-x)
            pixel-y (inverted-y-scale point-y)]
        (quil/fill red-value 0 blue-value 192)
        (quil/ellipse pixel-x
                      pixel-y
                      point-pixel-radius
                      point-pixel-radius)
        (draw-point-selection! pixel-x pixel-y
                               (inverted-x-scale mouse-x)
                               (inverted-y-scale mouse-y)
                               red-value blue-value)
        (quil/fill red-value 0 blue-value 255)))))

(defn draw-curves!
  "Draws the provided curves onto the current sketch."
  [curves x-scale y-scale opacity-scale x-pixel-min x-pixel-max]
  (doseq [{:keys [trace log-score]} curves]
    (let [f (trace/coefficient-function (trace/coefficients trace))]
      (quil/stroke 0 (opacity-scale log-score))
      (draw-plot f x-pixel-min x-pixel-max 10 x-scale y-scale))))

(defn draw-curve-count!
  "Draws the number of curves in the bottom right-hand corner"
  [curves pixel-width pixel-height]
  (let [curve-count (count curves)]
    (quil/rect-mode :corners)
    (quil/text-align :right :bottom)
    (quil/with-fill 0
      (quil/text-size 14) ; pixels
      (let [padding 5]
        (quil/text
         (str "curves: " curve-count)
         (- pixel-width padding)
         (- pixel-height padding))))))

(defn draw!
  "Draws the given state onto the current sketch."
  [{:keys [points curves mouse-pos]} x-scale y-scale pixel-width make-opacity-scale]
  (let [inverted-x-scale (scales/invert x-scale)
        inverted-y-scale (scales/invert y-scale)
        x-pixel-max (int (/ pixel-width 2))
        x-pixel-min (* -1 x-pixel-max)]
    (quil/background 255)
    (draw-curve-count! curves pixel-width pixel-height)
    (let [opacity-scale (make-opacity-scale (map :log-score curves))]
      (draw-curves! curves
                    inverted-x-scale
                    inverted-y-scale
                    opacity-scale
                    x-pixel-min
                    x-pixel-max))

    (draw-clicked-points! points curves mouse-pos inverted-x-scale inverted-y-scale)))
