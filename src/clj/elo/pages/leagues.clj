(ns elo.pages.leagues
  (:require [elo.db :as db]
            [elo.shared-config :as config]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "Leagues List")
   [:body
    [:div.github__auth
     [:a {:href "/oauth2/github"}
      "Authenticate with Github"]]

    [:div.league__content
     [:div.language_pick "Pick your League"]
     (into [:ul.list-group]
           (for [{:keys [id name game_type]} (db/load-leagues)]
             [:li.list-group-item
              [:img.league_logo_small {:width "30px"
                                       :src (config/logo (keyword game_type))}]
              [:a {:href (format "?league_id=%s" id)} name]]))]]])
