(ns byf.league-detail.games-list
  (:require [re-frame.core :as rf]
            [byf.common.players :as players-handlers]
            [byf.common.views :as common-views]
            [byf.league-detail.handlers :as handlers]
            [antizer.reagent :as ant]
            [reagent.core :as r]))

(def columns
  [{:title "game #"
    :dataIndex :game-idx}

   {:title "player"
    :dataIndex :player-1}

   {:title "team"
    :dataIndex :team-1}

   {:title "goals"
    :dataIndex :goals-1}

   {:title "player"
    :dataIndex :player-2}

   {:title "team"
    :dataIndex :team-2}

   {:title "goals"
    :dataIndex :goals-2}

   {:title "day"
    :dataindex :played-at}])

(defn rows
  []
  (let [games (rf/subscribe [::handlers/games-live-players])
        name-mapping (rf/subscribe [::players-handlers/name-mapping])
        up-to (rf/subscribe [::handlers/up-to-games])
        show-all? (rf/subscribe [::handlers/show-all?])]

    ))
