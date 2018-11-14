(ns equations.curve
  (:require
   [clojure.core.async :as async  :refer [<! <!! >! >!! alts! chan go go-loop]]
   [integrant.core :as integrant]))

(defn random-equation
  []
  [:equation/new {:a (- 2 (rand 4))
                  :b (+ 1 (rand-int 3))
                  :c (- 5 (rand 10))}])

(defn start-loop
  [in-chan out-chan]
  (let [stop-chan (async/chan)]
    (go-loop []
      (let [timeout-chan (async/timeout 500)
            [x ch] (alts! [in-chan timeout-chan stop-chan])]
        ;; recur
        ;; 5. in chan not nil
        ;; 1. timeout chan of any kind

        ;; no recur
        ;; 2. stop chan of any kind
        ;; 3. in chan nil
        ;; 4. out chan nil
        (when (or (and (= ch in-chan)
                       (some? x))
                  (= ch timeout-chan))
          (cond (= ch in-chan)
                (println "got point" x)

                (= ch timeout-chan)
                (do (println "timeout hit")
                    (>! out-chan (random-equation))
                    (println "wrote to out")))
          (recur))))
    stop-chan))
