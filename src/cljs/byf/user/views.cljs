(ns byf.user.views
  (:require [re-frame.core :as rf]
            [byf.league-detail.handlers :as detail-handlers]
            [byf.common.players :as players-handlers]
            [byf.common.views :as common-views]
            [byf.user.handlers :as handlers]))

;; sections that can be useful to add
;; - nemesis
;; - head to head
;; - suggested opponent
;; - personal stats
;; - best performing team

(defn head-to-head
  []
  (let [players (rf/subscribe [::players-handlers/players])
        head-to-head-wins (rf/subscribe [::handlers/head-to-head-wins])]
    (fn []
      ;; show win draws and losses
      [:div.form-group.opponent__input
       [common-views/drop-down-players players ::handlers/opponent]])))

(defn root
  []
  [:h2 "Hello Player detail page"
   [:div.user__root
    [head-to-head]]])
