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

(def pages
  {:league-list list-views/root
   :league-detail detail-views/root})

(defn root
  []
  (let [active-page @(rf/subscribe [:active-page])]
    [:div
     [header]
     [pages active-page]
     [footer]]))
