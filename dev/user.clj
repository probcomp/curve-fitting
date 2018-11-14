(ns user
  (:use [clojure.repl])
  (:require [integrant.core :as integrant]
            [integrant.repl :as repl :refer [clear go halt prep init reset reset-all]]
            [figwheel-sidecar.repl-api :as figwheel]
            [equations.server :as server]))

(figwheel/start-figwheel!)

(repl/set-prep! (constantly server/config))
