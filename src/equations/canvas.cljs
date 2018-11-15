(ns equations.canvas
  (:require
   [ajax.formats]
   [ajax.transit]
   [day8.re-frame.http-fx]
   [reagent.core  :as r]
   [re-frame.core :as rf]
   [equations.channels :as channels]
   [taoensso.encore :as encore :refer-macros [have have?]]
   [taoensso.timbre :as timbre :refer-macros [tracef debugf infof warnf errorf]]
   [taoensso.sente :as sente :refer [cb-success?]]
   [cljs.core.async :refer [<!]])
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer [go-loop]]))


(defn transform-context
  [ctx center-x center-y scale-x scale-y]
  (do
    (.translate ctx center-x center-y)
    (.scale ctx scale-x scale-y)
    ctx))

(defn add-relations
  [config]
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
           :scale-y (/ (.-height (:canvas c)) (:range-y c)))))

(defn make-graph
  []
  (let [canvas (.getElementById js/document "plot")
        graph  (add-relations {:canvas canvas
                               :min-x -10
                               :min-y -10
                               :max-x  10
                               :max-y  10
                               :units-per-tick 1})
        ctx (.getContext (:canvas graph) "2d")]

    (set! (.-globalCompositeOperation ctx) "multiply")
    (assoc graph :context
           (transform-context ctx
                              (:center-x graph)
                              (:center-y graph)
                              (:scale-x graph)
                              (:scale-y graph)))))

(defn add-equation!
  [graph equation opacity]
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

(defn add-point!
  [graph coords]
  (let [ctx (:context graph)
        [x-coord y-coord] coords.tail]

    (set! (.-globalAlpha ctx) 1.0)
    (.beginPath ctx)
    (.arc ctx x-coord y-coord (/ 3 (:scale-x graph)) 0 (* js/Math.PI 2) true)
    (.fill ctx)

    ctx))

(defn send-point!
  [coords]
  (js/console.warn "sending point" coords))

;; events

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:equations []
    :animate true
    :points []}))

(defn re-trigger-timer []
  (r/next-tick (fn [] (rf/dispatch [:timer]))))

(defn convert-scales [event graph]
  (let [rect (.getBoundingClientRect (:canvas graph))
        x-pixel-val (-
                     event.nativeEvent.clientX
                     (goog.object/get rect "left"))
        y-pixel-val (-
                     event.nativeEvent.clientY
                     (goog.object/get rect "top"))
        x-data-val (/ (-
                       x-pixel-val
                       (:center-x graph))
                      (:scale-x graph))
        y-data-val (/ (-
                       y-pixel-val
                       (:center-y graph))
                      (:scale-y graph))]
    [x-data-val y-data-val]))

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
 :rm-points
 (fn [{:keys [db] :as cofx} _]
   {:db (assoc-in db [:points] [])
    :http-xhrio {:method          :delete
                 :uri             "points"
                 :timeout         8000
                 :format          (ajax.transit/transit-request-format)
                 :response-format (ajax.formats/raw-response-format)
                 :on-success      [::rm-points-success]
                 :on-failure      [::rm-points-failure]}}))

(rf/reg-event-fx
 :click
 (fn [{:keys [db] :as cofx} [_ coords]]
   (let [x (first (goog.object/get coords "tail"))
         y (second (goog.object/get coords "tail"))]
     {:db (update-in db [:points] conj [x y])
      :http-xhrio {:method          :post
                   :uri             "point"
                   :params          {:x x, :y y}
                   :timeout         8000
                   :format          (ajax.transit/transit-request-format)
                   :response-format (ajax.formats/raw-response-format)
                   :on-success      [::point-post-success]
                   :on-failure      [::point-post-failure]}})))

(rf/reg-event-fx
 ::rm-points-success
 (fn [cofx [_ response]]
   (js/console.debug "Removing points success:" response)
   {}))

(rf/reg-event-fx
 ::rm-posts-failure
 (fn [cofx [_ response]]
   (js/console.error "Removing points failure:" response)
   {}))

(rf/reg-event-fx
 ::point-post-success
 (fn [cofx [_ response]]
   (js/console.debug "Point post success:" response)
   {}))

(rf/reg-event-fx
 ::point-post-failure
 (fn [cofx [_ response]]
   (js/console.error "Point post failure:" response)
   {}))

;; queries / subs

(rf/reg-sub
 :equations
 (fn [db _]
   (:equations db)))

(rf/reg-sub
 :animate
 (fn [db _]
   (:animate db)))

(rf/reg-sub
 :points
 (fn [db _]
   (:points db)))

;; views

(defn remove-points-button
  []
  [:button
   {:class "mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect"
    :on-click #(rf/dispatch [:rm-points])}
   "Remove Points"])

;; see [1] for an explanation. I'm not *sure* this pattern is required
;; here.
;; [1] https://github.com/Day8/re-frame/blob/master/docs/Using-Stateful-JS-Components.md
(defn plot-inner []
  (let [graph (atom nil)]

    (r/create-class
     {:reagent-render
      (fn []
        [:div
         [:canvas#plot
          {:width "600" :height "600"
           :on-click (fn [event] (rf/dispatch [:click (convert-scales event @graph)]))}]])

      :component-did-mount
      (fn [comp]
        (let [g (make-graph)]
          (re-trigger-timer)
          (reset! graph g)))

      :component-did-update
      (fn [comp]
        (let [{:keys [equations points]} (r/props comp)]

          (.clearRect (:context @graph)
                      (:min-x   @graph)
                      (:min-y   @graph)
                      (:range-x @graph)
                      (:range-y @graph))

          (run! #(add-equation! @graph (:equation %) (:opacity %))
                equations)

          (run! #(add-point! @graph %)
                points)))

      :display-name "plot-inner"})))

(defn plot-outer []
  (let [equations (rf/subscribe [:equations])
        points (rf/subscribe [:points])]
    (fn []
      [plot-inner {:equations @equations
                   :points @points}])))

(defn ui
  []
  [:main {:class "mdl-layout__content"}
   [:div {:class "mdl-grid"}
    [:div {:class "mdl-cell--12-col"}
     [:div
      [remove-points-button]]
     [:div {:style {:padding "16px"}}
      [plot-outer]]]]])

(def take-from-channel (atom true))

(defn start-channel-listener! []
  (go-loop []
    (let [[a b c] (<! channels/equation-channel)]
      (js/console.debug "Received " a b c)
      (rf/dispatch
       ;; a * x^b + c
       [:new-eq (fn [x]
                  (+ (* a
                        (js/Math.pow x b))
                     c))])
      (recur))))

(defn run
  []
  (rf/dispatch-sync [:initialize])
  (r/render [ui] (js/document.getElementById "app"))
  (start-channel-listener!))
