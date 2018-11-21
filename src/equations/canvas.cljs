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

(def axis-color "#40f0f0")
(def quadrille-color "#d0f0f0")

(defn draw-axes [graph]
  (let [ctx (.getContext (:canvas graph) "2d")
        [minx maxx miny maxy] ((juxt :min-x :max-x :min-y :max-y) graph)]
    (set! (.-globalAlpha ctx) 1.0)

    ;; quadrille
    (.beginPath ctx)
    (.setLineDash ctx [0.2 0.2])
    (set! (.-strokeStyle ctx) quadrille-color)
    (set! (.-lineWidth ctx) (/ 1 (:scale-x graph)))
    (doseq [y (range miny maxy)]
      (do
        (.moveTo ctx minx y)
        (.lineTo ctx maxx y)))
    (doseq [x (range minx maxx)]
      (do
        (.moveTo ctx x miny)
        (.lineTo ctx x maxy)))
    (.stroke ctx)
    (.setLineDash ctx [])

    ;; axes
    (.beginPath ctx)
    (.moveTo ctx (:min-x graph) 0)
    (.lineTo ctx (:max-x graph) 0)
    (.moveTo ctx 0 (:min-y graph))
    (.lineTo ctx 0 (:max-y graph))
    (set! (.-strokeStyle ctx) axis-color)
    (set! (.-lineWidth ctx) (/ 1 (:scale-x graph)))
    (.stroke ctx)
    ctx))


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
  [graph equation opacity score]
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
    :points []
    :worst-score 0
    :best-score 0}))

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

(defn new-opacity [opacity score worst best]
  (let [slowest 1
        fastest 8
        score-range (- best worst) ;; range of scores
        score-on-scale (- best score) ;; distance from best
        score-scale (/ 1.0 score-range) ;; multiplier to scale a scale on the range to a value 0 - 1
        scaled (* score-scale score-on-scale) ;; 0 - 1, low better
        speed  (if (= 0 score-range)
                 slowest
                 (+ slowest
                    (* scaled (- fastest slowest)))) ;; 1-10, low better
        ]
    (js/console.log "score" score "score-range" score-range "best" best "worst" worst "score-scale" score-scale "speed" speed)
    (- opacity speed)))

(rf/reg-event-db
 :timer
 (fn [db _]
   (if (:animate db)
     (do
       (re-trigger-timer)
       (assoc db
              :equations
              (map
               #(update % :opacity
                        (fn [op] (new-opacity op
                                              (:score %)
                                              (:worst-score db)
                                              (:best-score db))))
               (filter #(> (:opacity %) 1) (:equations db)))))
     db)))

(rf/reg-event-db
 :new-eq
 (fn [db [kw eq score]]
   (as-> db d
       (update-in d [:equations]
                  conj {:equation eq :opacity 100 :score score})

       (assoc-in d [:worst-score]
                 (or (reduce min
                             (map #(get % :score 0) (:equations d)))
                     0))

       (assoc-in d [:best-score]
                 (or (reduce max
                             (map #(get % :score 0) (:equations d)))
                     0)))))

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
          (draw-axes g)
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

          (draw-axes @graph)

          (run! #(add-equation! @graph
                                (:equation %)
                                (:opacity %)
                                (:score %))
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
    (let [[degree coeffs score] (<! channels/equation-channel)]

      (js/console.debug "Received %s %s %s" degree coeffs score)

      (rf/dispatch
       [:new-eq
        (fn [x]
          (reduce + (map
                     (fn [n] (* (nth coeffs n)
                                (js/Math.pow x n)))
                     (range degree))))
        score])
      (recur))))

(defn run
  []
  (rf/dispatch-sync [:initialize])
  (r/render [ui] (js/document.getElementById "app"))
  (start-channel-listener!))
