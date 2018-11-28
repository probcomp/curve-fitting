(ns curve-fitting.util.quil
  "Generic, domain-agnostic Quil utility functions."
  (:require [quil.core :as quil]))

(defmacro with-no-fill
  "Temporarily disable filling geometry for the body of this macro. The code
  outside of with-fill form will have the previous fill color set."
  [& body]
  `(let [previous-fill# (quil/current-fill)]
     (quil/no-fill)
     (let [return-val# (do ~@body)]
       (when (some? previous-fill#)
         (quil/fill previous-fill#))
       return-val#)))

(defn draw-circle
  "Draws a circle in the display window."
  [x y radius]
  (quil/ellipse x y radius radius))
