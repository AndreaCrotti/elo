(ns byf.league-list.views
  (:require [accountant.core :as accountant]
            [antizer.reagent :as ant]
            [byf.auth :as auth]
            [byf.league-list.handlers :as handlers]
            [byf.common.views :as common-views]
            [byf.routes :as routes]
            [byf.shared-config :as config]
            [re-frame.core :as rf]))

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
  (if (auth/logged-out?)
    [auth/authenticate]
    (do
      (rf/dispatch [::handlers/load-leagues])
      (fn []
        [:div.super
         [:div.section
          [league-picker]]
         [common-views/footer]]))))
