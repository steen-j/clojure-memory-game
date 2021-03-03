(ns memorygame.app
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defn ^:export run []
      (rdom/render [:<>] (js/document.getElementById "app")))

(defn ^:export reload []
      (js/console.log "reload...")
      (run))