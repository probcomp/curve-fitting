(ns curve-fitting.core
  (:require [metaprob.builtin :as metaprob]
            [metaprob.interpreters :as interpreters]
            [quil.applet :as applet]
            [quil.core :as quil]
            [quil.middleware :as middleware]
            [curve-fitting.model :as model]
            [curve-fitting.model.trace :as trace]
            [curve-fitting.inference :as inference]
            [curve-fitting.sketches.prior :as prior]
            [curve-fitting.sketches.resampling :as resampling]
            [curve-fitting.scales :as scales]))

(def text-padding 5) ; distance between text and scene border
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
  [pixel-x pixel-y radius r g b]
  (do
    (defn draw-circle []
      (quil/arc pixel-x
                pixel-y
                (* 2 radius)
                (* 2 radius)
                0
                (* 2 3.141)))
    (quil/fill 0 0 0 0)
    (quil/stroke-weight 4)
    (quil/stroke 255 255 255 192)
    (draw-circle)
    (quil/stroke-weight 1)
    (quil/stroke r g b 255)
    (draw-circle)))

(defn draw-point-borders
  [pixel-x pixel-y]
  "Draws a white border around a point to make it easier to distinguish between
  points and lines."
  (quil/fill 255 255 255 255)
  (quil/ellipse
   pixel-x pixel-y (+ 2 point-pixel-radius) (+ 2 point-pixel-radius)))

(defn draw-clicked-points!
  "Draws the given points onto the current sketch."
  [points curves inverted-x-scale inverted-y-scale]
  (quil/no-stroke)

  (let [trace-outliers (map trace/outliers (map :trace curves))
        outlier-scores (if (empty? trace-outliers)
                         (repeat (count points) 0)
                         (map #(/ (count (filter true? %))
                                  (count curves))
                              (apply map vector trace-outliers)))]
    (doseq [[point outlier-score] (map list
                                       points
                                       outlier-scores)]
      (let [point-mode (:outlier-mode point)
            [r g b] (case point-mode
                      :auto    [(int (* 255 outlier-score))
                                0
                                (- 255 (int (* 255 outlier-score)))]
                      :inlier  [0 255 0]
                      :outlier [255 0 255])
            pixel-x    (inverted-x-scale (:x point))
            pixel-y    (inverted-y-scale (:y point))]
        (draw-point-borders pixel-x pixel-y)
        (quil/fill r g b 255)
        (quil/ellipse pixel-x
                      pixel-y
                      point-pixel-radius
                      point-pixel-radius)

        (when (:selected point)
          (draw-point-selection! pixel-x pixel-y 10 r g b))

        (quil/fill r g b 255)))))

(defn draw-curves!
  "Draws the provided curves onto the current sketch."
  [curves x-scale y-scale opacity-scale x-pixel-min x-pixel-max]
  (doseq [{:keys [trace log-score]} curves]
    (let [f (trace/coefficient-function (trace/coefficients trace))]
      (quil/stroke 0 (opacity-scale log-score))
      (draw-plot f x-pixel-min x-pixel-max 10 x-scale y-scale))))

(defn draw-curve-count!
  "Draws the number of curves in the bottom right-hand corner"
  [curves max-curves digits pixel-width pixel-height]
  (let [curve-count (count curves)]
    (quil/rect-mode :corners)
    (quil/text-align :right :bottom)
    (quil/with-fill 0
      (quil/text-size 14) ; pixels
      (let [text (str "curves: " curve-count "/" (if (seq digits)
                                                   (str (apply str digits) "_")
                                                   max-curves))
            x (- pixel-width text-padding)
            y (- pixel-height text-padding)]
        (quil/text text x y)))))

(defn draw-mode!
  [mode pixel-width pixel-height]
  (quil/rect-mode :corners)
  (quil/text-align :left :bottom)
  (quil/with-fill 0
    (quil/text-size 14) ; pixels
    (quil/text (case mode
                 :prior "prior"
                 :resampling "approximate posterior")
               text-padding
               (- pixel-height text-padding))))

(defn draw!
  "Draws the given state onto the current sketch."
  [{:keys [mode points curves max-curves digits]} x-scale y-scale pixel-width pixel-height make-opacity-scale]
  (let [inverted-x-scale (scales/invert x-scale)
        inverted-y-scale (scales/invert y-scale)
        x-pixel-max (int (/ pixel-width 2))
        x-pixel-min (* -1 x-pixel-max)]
    (quil/background 255)
    (draw-mode! mode pixel-width pixel-height)
    (draw-curve-count! curves max-curves digits pixel-width pixel-height)
    (let [make-opacity-scale (case mode
                               :resampling resampling/make-opacity-scale
                               :prior prior/make-opacity-scale)
          opacity-scale (make-opacity-scale (map :log-score curves))]
      (draw-curves! curves
                    inverted-x-scale
                    inverted-y-scale
                    opacity-scale
                    x-pixel-min
                    x-pixel-max))

    (draw-clicked-points! points
                          curves
                          inverted-x-scale
                          inverted-y-scale)))
