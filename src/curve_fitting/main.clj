(ns curve-fitting.main
  (:require [integrant.core :as integrant]
            [curve-fitting.system :as system]))

(defn -main
  []
  (integrant/init system/config))
