(ns curve-fitting.system
  (:require [integrant.core :as integrant]
            [quil.applet :as applet]
            [curve-fitting.core :as core]))

(def config
  {::sketch core/sketch-config})

(defmethod integrant/init-key ::sketch
  [_ props]
  (apply applet/applet (into [] cat props)))

(defmethod integrant/halt-key! ::sketch
  [_ sketch]
  (.exit sketch))
