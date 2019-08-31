(ns byf.league-detail.games-list
  (:require [re-frame.core :as rf]
            [byf.common.players :as players-handlers]
            [byf.common.views :as common-views]
            [byf.league-detail.utils :refer [enumerate format-date]]
            [byf.league-detail.handlers :as handlers]
            [antizer.reagent :as ant]
            [reagent.core :as r]))

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
    :dataindex :played_at}])

(defn rows
  []
  (let [games @(rf/subscribe [::handlers/games-live-players])
        name-mapping @(rf/subscribe [::players-handlers/name-mapping])
        ;; up-to (rf/subscribe [::handlers/up-to-games])
        ;; show-all? (rf/subscribe [::handlers/show-all?])
        rev-games (-> games enumerate reverse)]

    (for [[idx {:keys [p1 p2] :as g}] rev-games]
      (merge
       (select-keys g
                    [:p1_using :p2_using :p1_points :p2_points :played_at])
       {:player-1 (get name-mapping p1)
        :player-2 (get name-mapping p2)
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
