(ns equations.channels
  (:require
   [cljs.core.async :refer [chan]]))

(defonce equation-channel (chan))
