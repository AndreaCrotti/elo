(ns byf.league-list.views
  (:require [accountant.core :as accountant]
            [clojure.string :as string]
            [byf.auth :as auth]
            [antizer.reagent :as ant]
            [byf.league-list.handlers :as handlers]
            [byf.common.handlers]
            [byf.common.views :as common-views]
            [byf.routes :as routes]
            [byf.shared-config :as config]
            [byf.utils :refer [classes]]
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
    [ant/list
     (for [{:keys [id name game_type]} @leagues]
       [ant/list-item {:key id}
        [:img {:width "70px"
               :src (config/logo (keyword game_type))}]

        [:a {:href "#"
             :on-click #(accountant/navigate!
                         (routes/path-for :league-detail :league-id id))}
         name]])]))

(defn root
  []
  (let [authenticated? @(rf/subscribe [::auth/authenticated?])]
    [:div.super
     #_[common-views/errors]
     (if authenticated?
       (do (rf/dispatch [::handlers/load-leagues])
           [:div.section
            [league-picker]])

       [:div
        [sign-in-block]])
     [common-views/footer]]))
