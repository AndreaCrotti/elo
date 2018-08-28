(ns elo.games
  (:require [elo.algorithms.elo :as elo]))

(defmulti normalize-game :game)

(defmethod normalize-game :fifa
  [{:keys [p1 p2 p1_goals p2_goals]}]
  (cond
    (= p1_goals p2_goals) [p1 p2 0.5 0.5]
    (> p1_goals p2_goals) [p1 p2 1 0]
    (> p2_goals p1_goals) [p1 p2 0 1]))

(defn player->ngames
  [games]
  (frequencies
   (flatten
    (for [g games]
      ((juxt :p1 :p2) g)))))

(defn get-rankings
  "Return all the rankings"
  [games players]
  (let [norm-games (->> games
                        (map #(assoc % :game :fifa))
                        (map normalize-game))
        rankings (elo/compute-rankings norm-games (map :id players))
        ngames (player->ngames games)]

    (reverse
     (sort-by :ranking
              (for [[k v] rankings]
                {:id k :ranking v :ngames (get ngames k 0)})))))
