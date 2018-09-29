(ns elo.games
  (:require [elo.algorithms.elo :as elo]))

(defmulti normalize-game :game)

(defmethod normalize-game :fifa
  [{:keys [p1 p2 p1_points p2_points]}]
  (cond
    (= p1_points p2_points) [p1 p2 0.5 0.5]
    (> p1_points p2_points) [p1 p2 1 0]
    (> p2_points p1_points) [p1 p2 0 1]))

(defn player->ngames
  [games]
  (frequencies
   (flatten
    (for [g games]
      ((juxt :p1 :p2) g)))))

(defn player->names
  "Transform players data into simpler id->name mapping"
  [players]
  (into {} (for [p players] {(:id p) (:name p)})))

(defn get-rankings
  "Return all the rankings"
  [games players]
  (let [norm-games (map elo/normalize-game games)
        rankings (elo/compute-rankings norm-games (map :id players))
        ngames (player->ngames games)]

    (reverse
     (sort-by :ranking
              (for [[k v] rankings]
                {:id k :ranking v :ngames (get ngames k 0)})))))
