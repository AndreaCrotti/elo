(ns elo.games
  (:require [elo.algorithms.elo :as elo]
            [clojure.core.specs.alpha :as s]
            [medley.core :as medley]))

(def default-results (zipmap [:losses :wins :draws] (repeat 0)))

(defmulti normalize-game :game)

(defmethod normalize-game :fifa
  [{:keys [p1 p2 p1_points p2_points]}]
  (cond
    (= p1_points p2_points) [p1 p2 0.5 0.5]
    (> p1_points p2_points) [p1 p2 1 0]
    (> p2_points p1_points) [p1 p2 0 1]))

(defn game-stats
  [{:keys [p1 p2 p1_points p2_points] :as game}]
  (let [points {:points-done p1_points :points-received p2_points}
        inv-points {:points-done p2_points :points-received p1_points}]
    (cond
      (= p1_points p2_points) [(merge {:player p1} points {:draws 1})
                               (merge {:player p2} points {:draws 1})]

      (> p1_points p2_points) [(merge {:player p1} points {:wins 1})
                               (merge {:player p2} inv-points {:losses 1})]

      (> p2_points p1_points) [(merge {:player p1} inv-points {:losses 1})
                               (merge {:player p2} points {:wins 1})])))

(defn regroup-games
  [games]
  (->> games
       (map game-stats)
       flatten
       (group-by :player)
       (medley/map-vals (fn [m] (map #(dissoc % :player) m)))))

(defn summarise
  [games]
  (->> games
       regroup-games
       (medley/map-vals #(apply merge-with + (cons default-results %)))))

(def kw-map {:wins :w :draws :d :losses :l})

(defn extract-result
  [game-map]
  (->> (select-keys game-map (keys kw-map))
       (medley.core/find-first (fn [[k v]] (pos? v)))
       first
       (get kw-map)))

(defn results
  [games]
  (->> games
       regroup-games
       (medley/map-vals #(map extract-result %))))

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

;;TODO: find a way to normalize the games at the boundaries so we can
;;more easily always use the same data structure internally

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

(defn result-str
  [game]
  (str (:p1 game) " vs " (:p2 game) ": "
       (:p1_points game) " - " (:p2_points game)))

(defn game-expanded
  "Expand the result of the game call"
  [rankings idx game]
  (let [norm-game (elo/normalize-game game)
        new-rankings (elo/new-rankings rankings norm-game)
        as-map (map (fn [[k v]] {:player k :ranking v}) new-rankings)]

    [new-rankings
     (map #(assoc %
                  :game idx
                  :result (result-str game)
                  :time (:played_at game))
          as-map)]))

(defn timeseries
  [games]
  (let [norm-games (map elo/normalize-game games)
        players (elo/extract-players norm-games)]

    (flatten
     (loop [idx 0
            rankings (elo/initial-rankings players)
            result []]

       (cond (= idx (count games)) result
             :else
             (let [[new-rankings res] (game-expanded rankings idx (nth games idx))]
               (recur (inc idx)
                      new-rankings
                      (conj result res))))))))
