(ns user
  (:use [clojure.repl])
  (:require [integrant.repl :as repl :refer [clear go halt init reset]]
            [integrant.repl.state :refer [system]]
            [curve-fitting.system :as system]))

(defn state
  []
  @(:state system))

(defn pprint-state
  []
  (clojure.pprint/pprint (state)))

(repl/set-prep! (constantly system/config))
