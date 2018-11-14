(ns user
  (:use [clojure.repl])
  (:require [integrant.core :as integrant]
            [integrant.repl :as repl :refer [clear go halt init prep reset reset-all resume suspend]]
            [integrant.repl.state :refer [system]]
            [figwheel-sidecar.repl-api :as figwheel]
            [equations.server :as server]))

(figwheel/start-figwheel!)

(repl/set-prep! (constantly server/config))
