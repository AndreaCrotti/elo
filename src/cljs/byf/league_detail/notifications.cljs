(ns byf.league-detail.notifications
  (:require [antizer.reagent :as ant]
            [re-frame.core :as rf]
            [byf.league-detail.handlers :as handlers]))

(defn notification
  [flag content]
  (when flag
    [:div
     [ant/alert {:type "info"
                 :message content}]]))

;; make this more generic to allow different position and different content
(defn add-user-notification
  []
  (let [show-notification (rf/subscribe [::handlers/add-user-notification])]
    (notification @show-notification
                  "Thank you, your game has been recorded")))

(defn current-user-notification
  []
  (let [current-user-set (rf/subscribe [::handlers/current-user-notification])]
    (notification @current-user-set
                  "Thanks, I'll remember it's you next time")))
