(ns curve-fitting.util.quil
  "Functioons for using Quil to draw points and lines."
  (:require [quil.core :as quil]))

(defn draw-point-borders
  [pixel-x pixel-y point-pixel-radius]
  "Draws a white border around a point to make it easier to distinguish between
  points and lines."
  (quil/fill 255 255 255 255)
  (quil/ellipse
   pixel-x pixel-y (+ 2 point-pixel-radius) (+ 2 point-pixel-radius)))
