(ns elo.league-list.views
  (:require [elo.shared-config :as config]
            [elo.routes :as routes]
            [elo.league-list.handlers :as handlers]
            [elo.utils :refer [classes]]
            [accountant.core :as accountant]
            [re-frame.core :as rf]))

(defn sign-up-button
  [provider]

  [:a {:class (classes ["btn" "btn-social" "btn-block" (str "btn-" provider)])}
   [:span {:class (classes ["fa" (str "fa-" provider)])}]
   (str "Sign in with " provider)])

(defn sign-in-block
  []
  (js/console.log "hello sign in block")
  [:div.sign-up__block
   (sign-up-button "github")
   #_(sign-up-button "google")])

(defn league-picker
  []
  (let [leagues (rf/subscribe [::handlers/leagues])]
    [:div.league__content
     [:div.language_pick "Pick your League"]
     (into [:ul.list-group]
           (for [{:keys [id name game_type]} @leagues]
             [:li.list-group-item
              [:img.league_logo_small {:width "30px"
                                       :src (config/logo (keyword game_type))}]

              [:a {:href "#"
                   :on-click #(accountant/navigate!
                               (routes/path-for :league-detail :league-id id))}
               name]]))]))

(defn root
  []
  (rf/dispatch [::handlers/load-leagues])
  [:div.league_list__root
   [league-picker]
   [sign-in-block]])
