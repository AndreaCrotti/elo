(ns elo.rankings
  "Functions to compute the rankings from the
  list of games"
  (:require [elo.games :as games]
            [medley.core :as medley]))

(defn truncate-games
  [games up-to-games]
  (if (some? up-to-games)
    (take up-to-games games)
    games))

(defn rankings-history
  [players visible-players games up-to]
  (let [visible-players-names (set (map :name visible-players))
        full-rankings
        (games/rankings-history players (truncate-games games up-to))]

    (->> full-rankings
         (filter #(contains? visible-players-names (:player %))))))

(defn domain
  [games players]
  (let [full-rankings-history (games/rankings-history players games)]
    [(apply min (map :ranking full-rankings-history))
     (apply max (map :ranking full-rankings-history))]))

(defn rankings
  [games players up-to-games dead-players game-config]
  (let [rankings
        (games/get-rankings (truncate-games games up-to-games)
                            players
                            game-config)

        updated (map #(if (contains? dead-players (:id %))
                        (assoc % :ranking 0) %)
                     rankings)]

    (sort-by #(- (:ranking %)) updated)))

(defn last-ranking-changes
  [rankings-history last-games-played-by]
  (medley/map-vals
   ;; also needs to take into consideration up-to-games??
   #(apply - (take 2
                   (reverse
                    (map :ranking (sort-by :game-idx %)))))

   (medley/filter-keys (or last-games-played-by #{})
                       (group-by :player rankings-history))))
