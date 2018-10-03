(ns elo.admin.views
  (:require [re-frame.core :as rf]
            [elo.common.views :refer [drop-down]]
            [elo.utils :as utils]))

(defn add-player-form
  []
  (let [valid-player? (rf/subscribe [:valid-player?])
        player (rf/subscribe [:player])
        league (rf/subscribe [:league])
        leagues (rf/subscribe [:leagues])]

    [:div.form-group.add-player_form
     [:div
      [drop-down @leagues :league]

      [:input.form-control {:type "text"
                            :value (:name @player)
                            :name "name"
                            :placeholder "John Smith"
                            :on-change (utils/set-val :name)}]

      [:input.form-control {:type "text"
                            :value (:email @player)
                            :name "email"
                            :placeholder "john.smith@email.com"
                            :on-change (utils/set-val :email)}]]

     [:div
      [:button {:type "button"
                :name "submit-game"
                :class (utils/classes ["submit__game" "btn" "btn-primary" (when-not @valid-player? "disabled")])
                :on-click (if @valid-player?
                            #(rf/dispatch [:add-player])
                            #(js/alert "Fill up the form first"))}

       "Register New Player"]]]))

(defn root
  []
  (rf/dispatch [:load-leagues])
  (fn []
    [:div.admin__page
     [add-player-form]]))
