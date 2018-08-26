(ns elo.pages.leagues
  (:require [elo.db :as db]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "Leagues List")
   [:body
    [:h2 "Pick your League"]
    (into [:ul.list-group]
          (for [{:keys [id name]} (db/load-leagues)]
            [:li.list-group-item
             [:a {:href (format "?league_id=%s" id)} name]]))]])
