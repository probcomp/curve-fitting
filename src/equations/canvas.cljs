(ns equations.canvas
  (:require
   [reagent.core  :as r]
   [re-frame.core :as rf]
   [equations.channels :as channels]
   [taoensso.encore :as encore :refer-macros (have have?)]
   [taoensso.timbre :as timbre :refer-macros (tracef debugf infof warnf errorf)]
   [taoensso.sente :as sente :refer (cb-success?)]
   [cljs.core.async :refer [<!]])
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go-loop)]))


(defn transform-context [ctx center-x center-y scale-x scale-y]
  (do
    (.translate ctx center-x center-y)
    (.scale ctx scale-x scale-y)
    ctx))

(defn add-relations [config]
  (do
    (as-> config c
      (assoc c
             :range-x (- (:max-x c) (:min-x c))
             :range-y (- (:max-y c) (:min-y c)))
      (assoc c
             :unit-x  (/ (:width (:canvas c)) (:range-x c))
             :unit-y  (/ (:height (:canvas c)) (:range-y c))
             :center-x (.round js/Math
                               (.abs js/Math (* (/ (:min-x c)
                                                   (:range-x c))
                                                (.-width (:canvas c)))))
             :center-y (.round js/Math
                               (.abs js/Math (* (/ (:min-y c)
                                                   (:range-y c))
                                                (.-height (:canvas c)))))
             :iteration (/ (:range-x c) 1000)
             :scale-x (/ (.-width (:canvas c)) (:range-x c))
             :scale-y (/ (.-height (:canvas c)) (:range-y c))))))


(defn make-graph []
  (let [canvas (.getElementById js/document "plot")
        graph  (add-relations {:canvas canvas
                               :min-x -10
                               :min-y -10
                               :max-x  10
                               :max-y  10
                               :units-per-tick 1})
        ctx (.getContext (:canvas graph) "2d")]
    (assoc graph :context
           (transform-context ctx
                              (:center-x graph)
                              (:center-y graph)
                              (:scale-x graph)
                              (:scale-y graph)))))

(defn add-equation! [graph equation opacity]
  (let [ctx (:context graph)]

    (set! (.-globalAlpha ctx) (/ opacity 100))
    (.beginPath ctx)
    (.moveTo ctx (:min-x graph) (equation (:min-x graph)))

    (loop [x (:min-x graph)]
      (when (< x (:max-x graph))
        (.lineTo ctx x (equation x))
        (recur (+ x (:iteration graph)))))

    (set! (.-lineJoin ctx) "round")
    (set! (.-lineWidth ctx) (/ 2 (:scale-x graph)))
    (set! (.-strokeStyle ctx) "grey")
    (.stroke ctx)

    ctx))

;; events

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:equations []
     :animate false}))

(defn re-trigger-timer []
  (r/next-tick (fn [] (rf/dispatch [:timer]))))

(rf/reg-event-db
  :timer
  (fn [db _]
    (if (:animate db)
      (do
        (re-trigger-timer)
        (assoc db
               :equations (map #(update % :opacity dec)
                               (filter #(> (:opacity %) 1) (:equations db)))))
      db)))

(rf/reg-event-db
 :new-eq
 (fn [db [_ eq]]
   (update-in db [:equations] conj {:equation eq :opacity 100})))

(rf/reg-event-fx
 :toggle-animation
 (fn [cofx _]
   (let [db        (:db cofx)
         animating (:animate db)
         disp      (if animating [] [:timer])]
     {:db       (assoc db :animate (not animating))
      :dispatch disp})))

;; queries / subs

(rf/reg-sub
 :equations
 (fn [db _]
   (:equations db)))

(rf/reg-sub
 :animate
 (fn [db _]
   (:animate db)))

;; views

(defn toggle-animation-button
  []
  (let [animate? @(rf/subscribe [:animate])
        text (if animate? "Stop animation" "Start animation")]
    [:button
     {:class "mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect"
      :on-click #(rf/dispatch [:toggle-animation])}
     text]))


(defn add-equation-button
  []
  [:button
   {:class "mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect"
    :on-click #(let [a (- 2 (rand 4))
                     b (+ 1 (rand-int 3))
                     c (- 5 (rand 10))]
                 (rf/dispatch
                  ;; a * x^b + c
                  [:new-eq (fn [x] (+ (* a (.pow js/Math x b)) c))]))}
   "Add equation"])

;; see [1] for an explanation. I'm not *sure* this pattern is required
;; here.
;; [1] https://github.com/Day8/re-frame/blob/master/docs/Using-Stateful-JS-Components.md
(defn plot-inner []
  (let [graph      (atom nil)
        update     (fn [comp]
                     (let [{:keys [equations]} (r/props comp)
                           canvas (:canvas @graph)]

                       (.clearRect (:context @graph)
                                   (:min-x   @graph)
                                   (:min-y   @graph)
                                   (:range-x @graph)
                                   (:range-y @graph))

                       (run!
                        #(add-equation!
                          @graph
                          (:equation %)
                          (:opacity %))
                        equations)))]

    (r/create-class
      {:reagent-render (fn []
                         [:div
                          [:canvas#plot {:width "600" :height "400"}]])

       :component-did-mount (fn [comp]
                              (let [g (make-graph)]
                                (reset! graph g)))

       :component-did-update update
       :display-name "plot-inner"})))

(defn plot-outer []
  (let [equations (rf/subscribe [:equations])]
    (fn []
      [plot-inner {:equations @equations}])))

(defn ui
  []
  [:main {:class "mdl-layout__content"}
   [:div {:class "mdl-grid"}
    [:div {:class "mdl-cell--12-col"}
     [:div
      [add-equation-button]
      [toggle-animation-button]]
     [:div {:style {:padding "16px"}}
      [plot-outer]]]]])

(def take-from-channel (atom true))
(defn start-channel-listener! []
  (go-loop []
    (let [[a b c]     (<! channels/equation-channel)]
      (.log js/console "Got some data " a  b c)
      (rf/dispatch
       ;; a * x^b + c
       [:new-eq (fn [x] (+ (* a (.pow js/Math x b)) c))])
      (recur)
      )))


(defn run
  []
  (rf/dispatch-sync [:initialize])
  (r/render [ui]
            (js/document.getElementById "app"))
  (start-channel-listener!))
