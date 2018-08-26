(ns elo.pages.leagues
  (:require [elo.db :as db]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "Leagues List")
   [:body
    [:div.league__content
     [:div.language_pick "Pick your League"]
     (into [:ul.list-group]
           (for [{:keys [id name]} (db/load-leagues)]
             [:li.list-group-item
              [:a {:href (format "?league_id=%s" id)} name]]))]]])
