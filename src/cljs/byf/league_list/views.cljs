(ns byf.league-list.views
  (:require [accountant.core :as accountant]
            [clojure.string :as string]
            [byf.auth :as auth]
            [byf.league-list.handlers :as handlers]
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
    [:div
     (into [:ol]
           (for [{:keys [id name game_type]} @leagues]
             [:li.league__name
              [:img {:width "70px"
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
            [:div.section
             [league-picker]])

        [:div
         [sign-in-block]]))))
