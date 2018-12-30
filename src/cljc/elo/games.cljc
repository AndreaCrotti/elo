(ns elo.games
  (:require [elo.algorithms.elo :as elo]
            #?(:clj [taoensso.timbre :as log])
            #?(:cljs [taoensso.timbre :as log :include-macros true])
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

(defn- plays?
  [game player-id]
  (contains? (set ((juxt :p1 :p2) game)) player-id))

#?(:clj (def fmt format))
#?(:cljs (def fmt goog.string/format))

(defn game-result
  [game name-mapping]
  (fmt "%s vs %s: (%d - %d)"
       (name-mapping (:p1 game))
       (name-mapping (:p2 game))
       (:p1_points game)
       (:p2_points game)))

(defn rankings-at-idx*
  [players idx all-games]
  (let [current-game (nth all-games idx)
        name-mapping (player->names players)
        common-map
        {:game-idx idx
         :time (:played_at current-game)
         :result (game-result current-game name-mapping)}

        rankings (get-rankings (take idx all-games) players)]

    (map #(merge % common-map)
         (for [r (filter #(plays? current-game (:id %)) rankings)]
           {:ranking (:ranking r)
            :player (name-mapping (:id r))}))))

(defn rankings-history
  [players games]
  (flatten
   (for [idx (range (count games))]
     (rankings-at-idx* players idx games))))

(defn longest-winning-subseq
  [s]
  (if-not (contains? (set s) :w)
    0
    (->> s
         (partition-by identity)
         (filter #(= #{:w} (set %)))
         (map count)
         (apply max))))

(defn- zipper
  [xs]
  (for [idx (range (dec (count xs)))]
    [(nth xs idx) (nth xs (inc idx))]))

(defn highest-increase-subseq
  [s]
  (loop [xs (zipper s)
         curr-increase 0
         max-increase 0]

    (let [[prev next] (first xs)
          rst (rest xs)
          new-max (max curr-increase max-increase)]

      (if (empty? xs)
        new-max
        (if (>= next prev)
          (recur rst (+ curr-increase (- next prev)) new-max)
          (recur rst 0 new-max))))))
