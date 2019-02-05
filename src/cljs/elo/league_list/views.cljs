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
  [:a {:href "/oauth2/github"}
   [:span {:class (classes ["fa" (str "fa-" provider)])}]
   (str "Sign in with " (string/capitalize provider))])

(defn sign-in-block
  []
  [:div
   [sign-up-button "github"]])

(defn league-picker
  []
  (let [leagues (rf/subscribe [::handlers/leagues])]
    [:div
     [:div "Pick your League"]
     (into [:ul]
           (for [{:keys [id name game_type]} @leagues]
             [:li
              [:img {:width "30px"
                     :src (config/logo (keyword game_type))}]

              [:a {:href "#"
                   :on-click #(accountant/navigate!
                               (routes/path-for :league-detail :league-id id))}
               name]]))]))

(defn root
  []
  (let [authenticated? (rf/subscribe [::auth/authenticated?])]
    (fn []
      (if @authenticated?
        (do (rf/dispatch [::handlers/load-leagues])
            [:div
             [league-picker]])

        [:div
         [sign-in-block]]))))
