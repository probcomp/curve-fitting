(ns equations.curve
  (:require
   [clojure.core.async :as async  :refer [<! <!! >! >!! alts! chan go go-loop]]
   [integrant.core :as integrant]
   [equations.generator]
   [metaprob.interpreters :refer [infer]]))

(defn random-equation
  []
  [:equation/new {:a (- 2 (rand 4))
                  :b (+ 1 (rand-int 3))
                  :c (- 5 (rand 10))}])

(defn make-obs-trace [obs]
  {"map" (zipmap (range (count obs))
                 (map (fn [o] {"gaussian" {:value o}}) obs))})

(defn new-polynomial [xs obs]
  (let [[result tr score] (infer
                           :procedure generator/curve-model
                           :inputs [xs]
                           :target-trace (make-obs-trace obs))
        coeffs (generator/get-coeffs-from-trace tr)]
    (println "score" score)
    {:degree (count coeffs)
     :coeffs coeffs
     :score score}))

;; (print (new-polynomial [-1 -0.5 0 0.5 1]
;;                        [0.2 0.4 0.6 0.8 1.0]))

(defn start-loop
  [in-chan out-chan]
  (let [stop-chan (async/chan)
        points    (atom [])]
    (go-loop []
      (let [timeout-chan (async/timeout 10000)
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
          (cond (and (= ch in-chan)
                     (= :new-point (first x)))
                (let [{:keys [x y]} (second x)]
                  (swap! points conj [x y]))

                (and (= ch in-chan)
                     (= :clear-points (first x)))
                (reset! points [])

                (= ch timeout-chan)
                (do (println "timeout hit")
                    (>! out-chan [:equations/start])
                    (dotimes [_ 200]
                      (>! out-chan [:equation/new
                                    (new-polynomial
                                     (map first @points)
                                     (map second @points))]))
                    (>! out-chan [:equations/end])
                    (println "wrote to out")
                    points))
          (recur))))
    stop-chan))
