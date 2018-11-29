(ns curve-fitting.point-sets
  (:require
   [curve-fitting.scales :as scales]))

(defn linear-points [x-min xs y-scale]
  (let [point-count  (count xs)
        y-min0       (:range-min y-scale)
        y-max0       (:range-max y-scale)
        y-indent     (/ (- y-max0 y-min0) 4)
        y-min        (+ y-min0 y-indent)
        y-max        (- y-max0 y-indent)
        y-range      (- y-max y-min)
        y-interval   (/ y-range (- point-count 1))]

    (map-indexed (fn [ix x] {:x x :y (+ y-min (* y-interval ix))
                             :selected false :outlier-mode :auto})
                 xs)))

(defn bump-points [x-min xs y-scale]
  (let [point-count  (count xs)
        y-min0       (:range-min y-scale)
        y-max0       (:range-max y-scale)
        y-indent-top (/ (- y-max0 y-min0) 50)
        y-indent-bottom (/ (- y-max0 y-min0) 3)
        y-min        (+ y-min0 y-indent-bottom)
        y-max        (- y-max0 y-indent-top)

        y-scale      (scales/->LinearScale 0 20 y-min y-max)
        ys           [6 7 6 5 4 3 2 19 2 3]]

    (map (fn [x y] {:x x :y (y-scale y)
                    :selected false :outlier-mode :auto})
                 xs ys)))

(defn linear-points-outlier [x-min xs y-scale]
  (let [points (vec (linear-points x-min xs y-scale))
        y-outlier (/ (- (:range-max y-scale) (:range-min y-scale)) 10)
        y-1    (- (get-in points [0 :y]) y-outlier)
        y-8    (+ (get-in points [9 :y]) y-outlier)]
    (-> points
        (assoc-in [1 :y] y-8)
        (assoc-in [8 :y] y-1))))

(defn exp-points [point-count x-scale y-scale]
  (let
      [x-max0       (:range-max x-scale)
       x-min0       (:range-min x-scale)
       x-indent-l   (/ (- x-max0 x-min0) 50)
       x-indent-r   (/ (- x-max0 x-min0) 3)
       x-min        (+ x-min0 x-indent-l)
       x-max        (- x-max0 x-indent-r)
       x-range      (- x-max x-min)
       x-interval   (/ x-range (- point-count 1))

       y-min0       (:range-min y-scale)
       y-max0       (:range-max y-scale)
       y-indent-top (/ (- y-max0 y-min0) 50)
       y-indent-bottom (/ (- y-max0 y-min0) 3)
       y-min        (+ y-min0 y-indent-bottom)
       y-max        (- y-max0 y-indent-top)

       xs (map-indexed (fn [ix interval]
                         (+ x-min (* ix interval)))
                       (repeat 10 x-interval))

       ys (map #(Math/pow (+ % (/ (- y-max0 y-min0) 2)) 2) xs)
       y-scale (scales/->LinearScale (first ys) (last ys) y-min y-max)]

    (map (fn [x y] {:x x :y (y-scale y)
                    :selected false :outlier-mode :auto})
         xs ys)))

(defn next-point-set
  [state x-scale y-scale]
  (let [point-count 10
        point-set-ix (:point-set state)

        x-max0       (:range-max x-scale)
        x-min0       (:range-min x-scale)
        x-indent     (/ (- x-max0 x-min0) 50)
        x-min        (+ x-min0 x-indent)
        x-max        (- x-max0 x-indent)
        x-range      (- x-max x-min)
        x-interval   (/ x-range (- point-count 1))

        xs (map-indexed (fn [ix interval]
                          (+ x-min (* ix interval)))
                        (repeat 10 x-interval))]
    [(case point-set-ix
       0 (linear-points x-min xs y-scale)
       1 (linear-points-outlier x-min xs y-scale)
       2 (exp-points point-count x-scale y-scale)
       3 (bump-points x-min xs y-scale))
     (mod (inc point-set-ix) 4)]))
