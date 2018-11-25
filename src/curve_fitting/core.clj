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

(defn draw-clicked-points!
  "Draws the given points onto the current sketch."
  [points inverted-x-scale inverted-y-scale]
  (quil/no-stroke)
  (quil/fill 255 0 0 192) ; red
  (doseq [[point-x point-y] points]
    (let [pixel-x (inverted-x-scale point-x)
          pixel-y (inverted-y-scale point-y)]
      (quil/ellipse pixel-x
                    pixel-y
                    point-pixel-radius
                    point-pixel-radius))))

(defn draw-curves!
  "Draws the provided curves onto the current sketch."
  [curves x-scale y-scale opacity-scale x-pixel-min x-pixel-max]
  (when (seq curves)
    (doseq [{:keys [trace score]} curves]
      (let [f (trace/coefficient-function (trace/coefficients trace))]
        (quil/stroke 0 (opacity-scale score))
        (draw-plot f x-pixel-min x-pixel-max 10 x-scale y-scale)))))

(defn draw!
  "Draws the given state onto the current sketch."
  [{:keys [points curves]} x-scale y-scale pixel-width]
  (let [inverted-x-scale (scales/invert x-scale)
        inverted-y-scale (scales/invert y-scale)
        x-pixel-max (int (/ pixel-width 2))
        x-pixel-min (* -1 x-pixel-max)]
    (quil/background 255)
    (let [opacity-scale (constantly 255)]
      (draw-curves! curves
                    inverted-x-scale
                    inverted-y-scale
                    opacity-scale
                    x-pixel-min
                    x-pixel-max))
    (draw-clicked-points! points inverted-x-scale inverted-y-scale)))
