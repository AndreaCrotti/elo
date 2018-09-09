(ns elo.league-list.views
  (:require [elo.shared-config :as config]
            [elo.routes :as routes]
            [accountant.core :as accountant]
            [re-frame.core :as rf]))

(defn root
  []
  (rf/dispatch-sync [:load-leagues])
  (let [leagues (rf/subscribe [:leagues])]
    [:div.league__content
     [:div.language_pick "Pick your League"]
     (into [:ul.list-group]
           (for [{:keys [id name game_type]} @leagues]
             [:li.list-group-item
              [:img.league_logo_small {:width "30px"
                                       :src (config/logo (keyword game_type))}]

              [:button {:on-click #(accountant/navigate!
                                    (routes/path-for :league-detail :league-id id))}
               name]]))]))
