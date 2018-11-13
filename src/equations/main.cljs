(ns equations.main
  (:require
   [equations.canvas :as canvas]
   [equations.ws :as ws]))

(enable-console-print!)

(if-let [el (.getElementById js/document "sente-csrf-token")]
  (do
    (.log js/console "CSRF token detected in HTML, great!")
    (ws/configure-chsk (.getAttribute el "data-csrf-token"))
    (ws/start-router!)
    (canvas/run))
  (.log js/console "CSRF token NOT detected in HTML, Sente cannot start"))
