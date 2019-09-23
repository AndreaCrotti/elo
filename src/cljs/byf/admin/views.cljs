(ns byf.admin.views
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [byf.admin.handlers :as handlers]
            [byf.common.views :refer [drop-down]]
            [byf.utils :as utils]))

(defn add-player-form
  []
  (let [valid-player? (rf/subscribe [::handlers/valid-player?])
        player (rf/subscribe [::handlers/player])
        leagues (rf/subscribe [::handlers/leagues])]

    (fn []
      [ant/form
       [ant/form-item
        [drop-down @leagues ::handlers/league (:league_id @player)
         :value-fn :id
         :display-fn :name]]

       [ant/form-item
        [ant/input-text-area
         {:value (:name @player)
          :name "name"
          :placeholder "John Smith"
          :on-change (utils/set-val ::handlers/name)}]]

       [ant/form-item
        [ant/input-text-area
         {:value (:email @player)
          :name "email"
          :placeholder "john.smith@email.com"
          :on-change (utils/set-val ::handlers/email)}]]

       [ant/form-item
        [ant/button {:type "primary"
                     :disabled (not @valid-player?)
                     :on-click (if @valid-player?
                                 #(rf/dispatch [::handlers/add-player])
                                 #(js/alert "Fill up the form first"))}

         "Register New Player"]]])))

(defn root
  []
  (rf/dispatch [::handlers/load-leagues])
  (fn []
    [add-player-form]))
