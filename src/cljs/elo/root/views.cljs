(ns elo.root.views
  (:require [re-frame.core :as rf]
            [elo.league-list.views :as list-views]
            [elo.league-detail.views :as detail-views]))

(defn header
  []
  [:div.header])

(defn footer
  []
  [:div.footer])

(defn get-page
  [page]
  (case page
    :league-list list-views/root
    :league-detail detail-views/root))

(defn root
  []
  (rf/dispatch-sync [:set-active-page :league-list])
  (let [active-page (rf/subscribe [:active-page])]
    (fn []
      (js/console.log "active page = " @active-page)
      [:div
       [header]
       [get-page @active-page]
       [footer]])))
