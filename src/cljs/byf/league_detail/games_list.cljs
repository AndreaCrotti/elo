(ns byf.league-detail.games-list
  (:require [re-frame.core :as rf]
            [byf.common.players :as players-handlers]
            [byf.league-detail.utils :refer [enumerate format-date]]
            [byf.league-detail.handlers :as handlers]
            [antizer.reagent :as ant]))

(def columns
  [{:title "game #"
    :dataIndex :game-idx}

   {:title "player"
    :dataIndex :player-1}

   {:title "team"
    :dataIndex :p1_using}

   {:title "goals"
    :dataIndex :p1_points}

   {:title "player"
    :dataIndex :player-2}

   {:title "team"
    :dataIndex :p2_using}

   {:title "goals"
    :dataIndex :p2_points}

   {:title "day"
    :dataIndex :played_at}])

(defn rows
  []
  (let [games @(rf/subscribe [::handlers/games-live-players])
        name-mapping @(rf/subscribe [::players-handlers/name-mapping])
        rev-games (-> games enumerate reverse)]

    (for [[idx {:keys [p1 p2] :as g}] rev-games]
      (merge
       (select-keys g
                    [:p1_using :p2_using :p1_points :p2_points])
       {:player-1 (get name-mapping p1)
        :player-2 (get name-mapping p2)
        :played_at (format-date (:played_at g))
        :game-idx idx}))))

(defn games-table
  []
  [ant/table
   {:columns    columns
    :dataSource (rows)
    :size       "small"
    :pagination true
    :loading    false
    :bordered   true
    :rowKey     :game-idx}])
