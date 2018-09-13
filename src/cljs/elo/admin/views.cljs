(ns elo.admin.views
  (:require [re-frame.core :as rf]
            [elo.common.views :refer [drop-down]]
            [elo.utils :as utils]))

;; there are two important use cases
;; 1. add a new user to the system just by email
;; 2. add an existing player from a company to a given league

(defn add-player-form
  []
  (let [valid-player? (rf/subscribe [:valid-player?])
        companies (rf/subscribe [:companies])
        company (rf/subscribe [:company])
        player (rf/subscribe [:player])]

    (js/console.log @companies " and " @company)
    [:div.form-group.add-player_form
     [:input.form-control
      #_[drop-down @companies :company "" #_@company
       :display-fn :name :value-fn :id]]

     [:div
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
  (rf/dispatch [:load-companies])
  (fn []
    [:div.admin__page
     [add-player-form]]))
