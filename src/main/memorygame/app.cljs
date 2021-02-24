(ns memorygame.app
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [memorygame.util :as util]))

(defonce current-skin (r/atom :default))
(defonce first-card (atom nil))
(defonce number-of-different-cards 8)
(defonce flip-style "rotateY(180deg)")
(def cards (r/atom {}))

(defn toggle-element-in-set [id key e]
  (swap! cards update-in [id key] #(util/set-toggle % e)))

(defn hide-cards [card-1 card-2]
  (toggle-element-in-set card-1 :classes "hidden")
  (toggle-element-in-set card-2 :classes "hidden"))

(defn flip-cards-back [card-1 card-2]
  (toggle-element-in-set card-1 :transform flip-style)
  (toggle-element-in-set card-2 :transform flip-style))

(defn flip-first-card [id]
  (toggle-element-in-set id :transform flip-style)
  (reset! first-card id))

(defn flip-second-card [id]
  (let [card @first-card]
    (toggle-element-in-set id :transform flip-style)
    (if (= (get-in @cards [card :image]) (get-in @cards [id :image]))
      (js/setTimeout #(hide-cards card id) 1000)
      (js/setTimeout #(flip-cards-back card id) 1000))
    (reset! first-card nil)))

(defn select-card [id]
  (when (not (= @first-card id))
    (if (not @first-card)
      (flip-first-card id)
      (flip-second-card id))))

(def base-skin (fn [{:keys [id image transform classes]} image-postfix]
                 [:div {:class :col}
                  [:div {:class    (into '(:flip-card) classes)
                         :on-click #(select-card id)}
                   [:div {:class :flip-card-inner :style {:transform transform}}
                    [:div {:class :flip-card-front}
                     [:img {:src (str "images/halloween-background" image-postfix ".png")}]]
                    [:div {:class :flip-card-back}
                     [:img {:src (str "images/halloween-" image image-postfix ".png")}]]]]]))

(def halloween-skin (fn [card] (base-skin card "")))

(def halloween-skin-reversed (fn [card] (base-skin card "-reversed")))

(defn target-value [event]
  (.-value (.-target event)))

(def render-fns {:default  halloween-skin
                 :reversed halloween-skin-reversed})

(defn control-panel []
  [:div
   [:select {:on-change #(reset! current-skin (keyword (target-value %)))}
    (doall (for [x (sort (keys render-fns))]
             ^{:key (name x)} [:option (name x)]))]
   [:button {:on-click #(reset! cards (util/setup-new-game number-of-different-cards))} "Nyt spil"]])

(defn render-cards [c]
  (for [x (vals c)]
    (do
      ^{:key (:id x)} [(get-in render-fns [@current-skin]) x])))


(defn mini-app []
  [:div
   [:div {:style {:width :1110px :display :inline-block}}
    [:div {:class [:flip-card-container :flex-grid]}
     (render-cards @cards)]]
   [:div {:style {:display :inline-block}}
    (control-panel)]
   ])

(defn ^:export run []
  (reset! cards (util/setup-new-game number-of-different-cards))
  (rdom/render [mini-app] (js/document.getElementById "app")))

(defn ^:export reload []
  (js/console.log "reload...")
  (run))