(ns user
  (:use [clojure.repl])
  (:require [integrant.repl :as repl :refer [clear go halt init reset]]
            [integrant.repl.state :refer [system]]
            [curve-fitting.system :as system]))

(repl/set-prep! (constantly system/config))
