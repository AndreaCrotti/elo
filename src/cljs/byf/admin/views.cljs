(ns byf.admin.views
  (:require [re-frame.core :as rf]
            [byf.admin.handlers :as handlers]
            [byf.common.views :refer [drop-down]]
            [byf.elements :as el]
            [byf.utils :as utils]))

(defn add-player-form
  []
  (let [valid-player? (rf/subscribe [::handlers/valid-player?])
        player (rf/subscribe [::handlers/player])
        leagues (rf/subscribe [::handlers/leagues])]

    (fn []
      [:div.section
       [:div
        [drop-down @leagues ::handlers/league (:league_id @player)
         :value-fn :id
         :display-fn :name]

        [el/input
         [:is-fullwidth]
         {:type "text"
          :value (:name @player)
          :name "name"
          :placeholder "John Smith"
          :on-change (utils/set-val ::handlers/name)}]

        [el/input
         [:is-fullwidth]
         {:type "text"
          :value (:email @player)
          :name "email"
          :placeholder "john.smith@email.com"
          :on-change (utils/set-val ::handlers/email)}]]

       [:div
        [el/button
         [:is-primary :is-fullwidth (when-not @valid-player? "disabled")]
         {:type "button"
          :name "submit-game"
          :on-click (if @valid-player?
                      #(rf/dispatch [::handlers/add-player])
                      #(js/alert "Fill up the form first"))}

         "Register New Player"]]])))

(defn root
  []
  (rf/dispatch [::handlers/load-leagues])
  (fn []
    [:div
     [add-player-form]]))
