(ns memorygame.app
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [memorygame.util :as util]))

(defonce current-skin (r/atom :default))
(defonce first-card (atom nil))
(defonce number-of-different-cards 8)
(defonce rounds (r/atom 0))
(defonce flip-style "rotateY(180deg)")
(defonce player-one-score (r/atom 0))
(defonce player-two-score (r/atom 0))
(defonce current-player (r/atom :player-one))
(defonce cards (r/atom {}))

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

(defn swap-player! [whos-up]
      (if (= :player-one whos-up)
        (reset! current-player :player-two)
        (reset! current-player :player-one)))

(defn increase-score-for-player! [whos-up]
      (if (= :player-one whos-up)
        (swap! player-one-score inc)
        (swap! player-two-score inc)))

(defn flip-second-card [id]
      (let [card @first-card]
           (toggle-element-in-set id :transform flip-style)
           (if (= (get-in @cards [card :image]) (get-in @cards [id :image]))
             (do
               (increase-score-for-player! @current-player)
               (js/setTimeout #(hide-cards card id) 1000)
               (when (= (+ @player-one-score @player-two-score) number-of-different-cards)
                     (js/setTimeout
                       #(js/alert (str
                                    (cond
                                      (< @player-two-score @player-one-score) "Player 1"
                                      (< @player-one-score @player-two-score) "Player 2"
                                      :else "Ingen") " vandt!"
                                    )) 1500)))
             (do
               (swap-player! @current-player)
               (js/setTimeout #(flip-cards-back card id) 1000)))
           (reset! first-card nil)
           (swap! rounds inc)))

(defn select-card [id firstcard]
      (when (not (= firstcard id))
            (if (not firstcard)
              (flip-first-card id)
              (flip-second-card id))))

(defn base-skin [{:keys [id image transform classes] :as card} image-postfix]
      [:div {:class :col}
       [:div {:class    (into '(:flip-card) classes)
              :on-click #(select-card id @first-card)}
        [:div {:class :flip-card-inner :style {:transform transform}}
         [:div {:class :flip-card-front}
          [:img {:src (str "images/halloween-background" image-postfix ".png")}]]
         [:div {:class :flip-card-back}
          [:img {:src (str "images/halloween-" image image-postfix ".png")}]]]]])

(defn halloween-skin [card] (base-skin card ""))
(defn halloween-skin-reversed [card] (base-skin card "-reversed"))
(def render-fns {:default  halloween-skin
                 :reversed halloween-skin-reversed})

(defn target-value [event]
      (.-value (.-target event)))

(defn control-panel []
      [:div {:class :control-area}
       [:div
        [:table {:style {:width :100%}}
         [:tbody
          [:tr
           [:td "Skin"]
           [:td [:select {:on-change #(reset! current-skin (keyword (target-value %)))}
                 (doall (for [x (sort (keys render-fns))]
                             ^{:key (name x)} [:option (name x)]))]]]
          [:tr
           [:td "Nyt spil"]
           [:td [:button {:on-click #((reset! first-card nil)
                                      (reset! cards (util/setup-new-game number-of-different-cards)))}
                 "Nyt spil"]]]
          [:tr [:td "Runde"]
           [:td @rounds]]
          [:tr
           [:td "Aktiv spiller"]
           [:td (name @current-player)]]
          [:tr
           [:td "Score"]]
          [:tr
           [:td "Player 1"]
           [:td @player-one-score]]
          [:tr
           [:td "Player 2"]
           [:td @player-two-score]]]]]])

(defn render-cards [c skin]
     (for [x (vals c)]
            ^{:key (:id x)} [(get-in render-fns [skin]) x])
      #_(map #(^{:key (:id x)} (get-in render-fns [skin] %)) (vals c))
      )

(defn mini-app []
      [:div
       [:div {:style {:width :1110px :display :inline-block}}
        [:div {:class [:flip-card-container :flex-grid]}
         (render-cards @cards @current-skin)]]
       [:div {:style {:display :inline-block :vertical-align :top :width :250px}}
        (control-panel)]
       ])

(defn ^:export run []
      (reset! cards (util/setup-new-game number-of-different-cards))
      (rdom/render [mini-app] (js/document.getElementById "app")))

(defn ^:export reload []
      (js/console.log "reload...")
      (run))