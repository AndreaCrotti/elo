(ns byf.league-detail.add-game
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [byf.shared-config :as config]
            [byf.common.views :as common-views]
            [byf.utils :as utils]
            [byf.common.players :as players-handlers]
            [byf.league-detail.handlers :as handlers]
            [byf.league-detail.handlers.add-game :as add-handlers]
            [cljsjs.moment]))

(def form-config
  {:class "add-game-form"
   :layout "horizontal"
   ;; :label-col {:xs {:span 24}
   ;;             :sm {:span 6}}
   ;; :wrapper-col {:xs {:span 24}
   ;;               :sm {:span 6}}
})

(defn- enable-button
  [valid-game? opts]
  (if valid-game?
    opts
    (assoc opts :disabled "{true}")))

(defn team-input
  [val handler]
  [ant/input-text-area
   {:default-value val
    :value val
    :autosize {:max-rows 1}
    :on-change (utils/set-val handler)}])

(defn game-form
  []
  (rf/dispatch [::players-handlers/load-players])
  (fn []
    (let [players (rf/subscribe [::add-handlers/active-players-full])
          valid-game? (rf/subscribe [::add-handlers/valid-game?])
          game (rf/subscribe [::add-handlers/game])
          league (rf/subscribe [::handlers/league])
          game-type (or (:game_type @league) :fifa)
          points-range (map str (config/opts game-type :points))
          sorted-players (sort-by :name @players)]

      [ant/form form-config
       [ant/form-item {:label "Player 1"}
        [common-views/drop-down-players sorted-players ::add-handlers/p1 (:p1 @game)]]

       [ant/form-item {:label "Goals"}
        [common-views/drop-down points-range ::add-handlers/p1_points (:p1_points @game)]]

       [ant/form-item {:label "Team 1"}
        [team-input (:p1_using @game) ::add-handlers/p1_using]]

       [ant/form-item {:label "Player 2"}
        [common-views/drop-down-players sorted-players ::add-handlers/p2 (:p2 @game)]]

       [ant/form-item {:label "Goals"}
        [common-views/drop-down points-range ::add-handlers/p2_points (:p2_points @game)]]

       [ant/form-item {:label "Team 2"}
        [team-input (:p2_using @game) ::add-handlers/p2_using]]

       [ant/form-item {:label "Played At"}
        ;; link to the right value here
        [ant/date-picker {:show-time true
                          :format "YYYY-MM-DD HH:mm"
                          :value (:played_at @game)
                          :on-change
                          (fn [mo _]
                            (rf/dispatch [::add-handlers/played_at mo]))}]]

       [ant/form-item
        [ant/button
         (enable-button @valid-game?
                        {:type "primary"
                         :on-click #(rf/dispatch [::add-handlers/add-game])})
         "Add Game"]]])))
