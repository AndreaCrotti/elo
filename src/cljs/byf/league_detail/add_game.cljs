(ns byf.league-detail.add-game
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [byf.shared-config :as config]
            [byf.common.views :as common-views]
            [byf.utils :as utils]
            [byf.common.players :as players-handlers]
            [byf.league-detail.handlers :as handlers]
            [cljsjs.moment]))

(defn- translate
  [term]
  (let [league (rf/subscribe [::handlers/league])]
    ;;XXX: is there a way to avoid all this extra safety?
    (config/term (or (:game_type @league) :fifa) term)))

(defn- enable-button
  [valid-game? opts]
  (if valid-game?
    opts
    (assoc opts :disabled "{true}")))

(defn team-input
  [val handler]
  [ant/input-text-area
   {:default-value val
    :autosize {:max-rows 1}
    :on-change (utils/set-val handler)}])

(defn game-form
  []
  (let [players (rf/subscribe [::players-handlers/players])
        valid-game? (rf/subscribe [::handlers/valid-game?])
        game (rf/subscribe [::handlers/game])
        league (rf/subscribe [::handlers/league])
        game-type (or (:game_type @league) :fifa)
        points-range (map str (config/opts game-type :points))
        sorted-players (sort-by :name @players)]

    [ant/form {:layout "vertical"}
     [ant/form-item {:label "Player 1"}
      [common-views/drop-down-players sorted-players ::handlers/p1 (:p1 @game)]]

     [ant/form-item {:label "Goals"}
      [common-views/drop-down points-range ::handlers/p1_points (:p1_points @game)]]

     [ant/form-item {:label "Team 1"}
      [team-input (:p1_using @game) ::handlers/p1_using]]

     [ant/form-item {:label "Player 2"}
      [common-views/drop-down-players sorted-players ::handlers/p2 (:p2 @game)]]

     [ant/form-item {:label "Goals"}
      [common-views/drop-down points-range ::handlers/p2_points (:p2_points @game)]]

     [ant/form-item {:label "Team 2"}
      [team-input (:p2_using @game) ::handlers/p2_using]]

     [ant/form-item {:label "Played At"}
      ;; link to the right value here
      [ant/date-picker {:show-time true
                        :format "YYYY-MM-DD HH:mm"
                        :default-value (js/moment)
                        :on-change
                        (fn [mo mo-str]
                          (rf/dispatch [::handlers/played_at mo-str]))}]]

     [ant/form-item
      [ant/button
       #_(enable-button @valid-game?)
       {:on-click #(do
                     (js/console.log "adding game")
                     (rf/dispatch [::handlers/add-game]))}

       "Add Game"]]]))
