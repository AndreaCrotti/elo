(ns elo.league-list.views
  (:require [accountant.core :as accountant]
            [clojure.string :as string]
            [elo.auth :as auth]
            [elo.league-list.handlers :as handlers]
            [elo.routes :as routes]
            [elo.shared-config :as config]
            [elo.utils :refer [classes]]
            [re-frame.core :as rf]))

(defn sign-up-button
  "Generate a generic sign up button"
  [provider]
  [:a {:class (classes ["btn" "btn-social" "btn-block" (str "btn-" provider)])
       ;; TODO: move this to routing as well
       :href "/oauth2/github"}

   [:span {:class (classes ["fa" (str "fa-" provider)])}]
   (str "Sign in with " (string/capitalize provider))])

(defn sign-in-block
  []
  [:div.sign-up__block
   [sign-up-button "github"]])

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

              [:a {:href (routes/path-for :league-detail :league-id id)}
               name]]))]))

(defn root
  []
  (let [authenticated? (rf/subscribe [::auth/authenticated?])]
    (fn []
      (if @authenticated?
        (do (rf/dispatch [::handlers/load-leagues])
            [:div.league_list__root
             [league-picker]])

        [:div.auth__root
         [sign-in-block]]))))
