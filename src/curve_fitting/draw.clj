(ns curve-fitting.draw
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
            [curve-fitting.scales :as scales]
            [curve-fitting.util.quil :as util.quil]))

(def text-padding 5) ; distance between text and scene border
(def point-pixel-radius 12)
(def plot-step 1) ; x pixel width between curve points

(defn draw-point-border!
  [x y radius]
  "Draws a white border around a point to make it easier to distinguish between
  points and lines."
  (quil/with-fill [255 255 255 255]
    (quil/ellipse
     x y (+ 2 radius) (+ 2 radius))))

(defn draw-point!
  "Draws a point and its corresponding white border."
  [pixel-x pixel-y point-pixel-radius red-value blue-value]
  (draw-point-border! pixel-x pixel-y point-pixel-radius)
  (quil/fill red-value 0 blue-value 190)
  (quil/ellipse pixel-x
                pixel-y
                point-pixel-radius
                point-pixel-radius))

(defn draw-point-selection!
  [pixel-x pixel-y radius r g b]
  (quil/stroke-weight 4)
  (quil/with-stroke [255 255 255 192]
    (util.quil/with-no-fill
      (util.quil/draw-circle pixel-x pixel-y radius)))

  (quil/stroke-weight 1)
  (quil/with-stroke [r g b 255]
    (util.quil/with-no-fill
      (util.quil/draw-circle pixel-x pixel-y radius))))

(defn draw-plot [f px-pt-scales]
  (quil/no-fill)
  (quil/begin-shape)

  (let [{x-px-pt :x, y-px-pt :y} px-pt-scales
        x-px-min (:domain-min x-px-pt)
        x-px-max (:domain-max x-px-pt)
        y-pt-px (scales/invert y-px-pt)]
    (doseq [[x y] (->> (range x-px-min x-px-max plot-step)
                       (map (fn [x]
                              (let [y (f (x-px-pt x))]
                                [x (y-pt-px y)]))))]
      (quil/curve-vertex x y)))

  (quil/end-shape))

(defn draw-clicked-points!
  "Draws the given points onto the current sketch."
  [points curves px-pt-scales]
  (quil/no-stroke)
  (let [{x-px-pt :x, y-px-pt :y} px-pt-scales
        x-pt-px (scales/invert x-px-pt)
        y-pt-px (scales/invert y-px-pt)
        trace-outliers (map trace/outliers (map :trace curves))
        outlier-scores (if (empty? trace-outliers)
                         (repeat (count points) 0)
                         (map #(/ (count (filter true? %))
                                  (count curves))
                              (apply map vector trace-outliers)))]
    (doseq [[point outlier-score] (map list points outlier-scores)]
      (let [point-mode (:outlier-mode point)
            [r g b] (case point-mode
                      :auto    [(int (* 255 outlier-score))
                                0
                                (- 255 (int (* 255 outlier-score)))]
                      :inlier  [0 255 0]
                      :outlier [255 0 255])
            pixel-x (x-pt-px (:x point))
            pixel-y (y-pt-px (:y point))]
        (draw-point-border! pixel-x pixel-y point-pixel-radius)
        (quil/with-fill [r g b 255]
          (util.quil/draw-circle pixel-x pixel-y point-pixel-radius))

        (when (:selected point)
          (draw-point-selection! pixel-x pixel-y (+ point-pixel-radius 3) r g b))

        (quil/fill r g b 255)))))

(defn draw-curves!
  "Draws the provided curves onto the current sketch."
  [curves px-pt-scales opacity-scale]
  (doseq [{:keys [trace log-score]} curves]

    (let [{x-px-pt :x, y-px-pt :y} px-pt-scales
          y-pt-px (scales/invert y-px-pt)
          il (Math/abs (y-pt-px (trace/inlier-noise trace)))
          ol (Math/abs (y-pt-px (trace/outlier-noise trace)))
          f (trace/coefficient-function (trace/coefficients trace))
          _ (println "il" il "ol" ol)]

      (quil/stroke 200 30 64 40)
      (quil/stroke-weight ol)
      (draw-plot f px-pt-scales)

      (quil/stroke 30 200 64 40)
      (quil/stroke-weight il)
      (draw-plot f px-pt-scales)

      (quil/stroke-weight 1)
      (quil/stroke 0 (opacity-scale log-score))
      (draw-plot f px-pt-scales))))

(defn draw-curve-count!
  "Draws the number of curves in the bottom right-hand corner"
  [curves max-curves digits px-pt-scales]
  (let [curve-count (count curves)
        px-width (scales/domain-size (:x px-pt-scales))
        px-height (scales/domain-size (:y px-pt-scales))]
    (quil/rect-mode :corners)
    (quil/text-align :right :bottom)
    (quil/with-fill 0
      (quil/text-size 14) ; pixels
      (let [text (str "curves: " curve-count "/" (if (seq digits)
                                                   (str (apply str digits) "_")
                                                   max-curves))
            x (- px-width text-padding)
            y (- px-height text-padding)]
        (quil/text text x y)))))

(defn draw-mode!
  [mode outliers? px-pt-scales]
  (quil/rect-mode :corners)
  (quil/text-align :left :bottom)
  (let [px-height (scales/domain-size (:y px-pt-scales))]
    (quil/with-fill 0
      (quil/text-size 14) ; pixels
      (quil/text (str (case mode
                        :prior "prior"
                        :resampling "approximate posterior")
                      " with outliers "
                      (if outliers? "enabled" "disabled"))
                 text-padding
                 (- px-height text-padding)))))

(defn draw!
  "Draws the given state onto the current sketch."
  [{:keys [mode outliers? points curves max-curves digits]} px-pt-scales]
  (quil/background 255)
  (draw-mode! mode outliers? px-pt-scales)
  (draw-curve-count! curves max-curves digits px-pt-scales)
  (let [make-opacity-scale (case mode
                             :resampling resampling/make-opacity-scale
                             :prior prior/make-opacity-scale)
        opacity-scale (make-opacity-scale (map :log-score curves))]
    (draw-curves! curves px-pt-scales opacity-scale))
  (draw-clicked-points! points curves px-pt-scales))
