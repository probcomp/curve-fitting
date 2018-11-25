(ns curve-fitting.core
  (:require [metaprob.builtin :as metaprob]
            [metaprob.interpreters :as interpreters]
            [quil.applet :as applet]
            [quil.core :as quil]
            [quil.middleware :as middleware]
            [curve-fitting.model :as model]
            [curve-fitting.inference :as inference]
            [curve-fitting.scales :as scales]))

(def pixel-width 500)
(def pixel-height pixel-width)
(def x-pixel-max (int (/ pixel-width 2)))
(def x-pixel-min (* -1 x-pixel-max))

(def x-point-min -10)
(def x-point-max 10)
(def point-width (- x-point-max x-point-min))
(def y-point-min x-point-min)
(def y-point-max x-point-max)
(def point-height (- y-point-max y-point-min))

(def point-pixel-radius 8)
(def anti-aliasing 8)
(def num-curves 10)

(def x-scale (scales/linear [0 pixel-width] [x-point-min x-point-max]))
(def y-scale (scales/linear [pixel-height 0] [y-point-min y-point-max]))
(def inverted-x-scale (scales/invert x-scale))
(def inverted-y-scale (scales/invert y-scale))

(def num-particles 150)

(defn degree
  "Returns the degree from a trace of `curve-fitting.model/curve-model`."
  [trace]
  (metaprob/trace-get
   (metaprob/trace-subtrace
    trace
    '(1 1 "generate-curve" 0 "degree" "uniform-sample"))))

(defn coefficients
  "Returns the coefficients from a trace of
  `curve-fitting.model/curve-model`."
  [trace]
  (let [coeff-subtraces (metaprob/trace-subtrace
                         trace
                         '(1 1 "generate-curve" 1 "coeffs" "replicate" "map"))]
    (map (fn [i]
           (metaprob/trace-get
            (metaprob/trace-subtrace
             coeff-subtraces
             (list i "f" "gaussian"))))
         (range (count (metaprob/trace-keys coeff-subtraces))))))

(defn target-trace
  "Returns a target trace that fixes the choices for `y` values in
  `curve-fitting.model/curve-model` to the provided values."
  [ys]
  {"map"
   (into {} (map-indexed (fn [i y]
                           {i {"gaussian" {:value y}}})
                         ys))})

(defn init
  "Returns the initial state for the sketch."
  []
  {:points [], :curves []})

(defn draw-plot [f from to step]
  (quil/no-fill)
  (quil/begin-shape)
  (doseq [[x y] (->> (range from to step)
                     (map (fn [x]
                            (let [y (f x)]
                              [(inverted-x-scale x)
                               (inverted-y-scale y)]))))]
    (quil/curve-vertex x y))
  (quil/end-shape))

(defn coefficient-function
  [coefficients]
  (fn [x]
    (->> coefficients
         (map-indexed (fn [i coefficient]
                        (* coefficient (Math/pow x i))))
         (reduce +))))

(defn draw-clicked-points!
  "Draws the given points onto the current sketch."
  [points]
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
  [curves opacity-scale]
  (when (seq curves)
    (doseq [{:keys [f score]} curves]
      (quil/stroke 0 (opacity-scale score))
      (draw-plot f x-pixel-min x-pixel-max 10))))

(defn draw!
  "Draws the given state onto the current sketch."
  [state]
  (fn [_]
    (let [{:keys [points curves]} @state]
      (quil/background 255)
      (let [opacity-scale (constantly 255)]
        (draw-curves! curves opacity-scale))
      (draw-clicked-points! points))))

(defn points-to-curve
  [points]
  (let [xs (map first points)
        ys (map second points)]
    (let [[trace score] (inference/importance-resampling
                         model/curve-model
                         [xs]
                         (target-trace ys)
                         num-particles)]
      {:f (coefficient-function (coefficients trace))
       :score score})))

(defn points-to-curves
  [points]
  (let [xs (map first points)
        ys (map second points)
        curves (for [[trace score] (repeatedly num-curves
                                               #(inference/importance-resampling
                                                 model/curve-model
                                                 [xs]
                                                 (target-trace ys)
                                                 num-particles))]
                 {:f (coefficient-function (coefficients trace))
                  :score (Math/exp score)})
        score-sum (transduce (map :score) + curves)]
    (mapv #(update % :score (fn [score] (/ score score-sum)))
          curves)))

(defn mouse-pressed
  [state]
  (fn [_ {:keys [x y]}]
    (swap! state (fn [state]
                   (-> state
                       (update :points conj [(x-scale x) (y-scale y)])
                       (assoc :curves []))))))

(defn key-typed
  [state]
  (fn [_ {:keys [raw-key]}]
    (if (contains? #{\backspace} raw-key)
      (reset! state (init))
      (swap! state assoc :curves []))))

(defn settings
  []
  (quil/smooth anti-aliasing))
