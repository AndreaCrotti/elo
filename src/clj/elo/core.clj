(ns elo.core)

(def k 32)
(def initial-ranking 1500)

(defn expected
  [diff]
  (/ 1 (inc (Math/pow 10 (/ diff 400)))))

(defn new-rating
  [old expected game]
  (+ old (* k (- game expected))))

(defn new-ratings
  [ratings [p1 p2 game]]

  (let [ra (get ratings p1)
        rb (get ratings p2)]

    (assoc ratings
           p1 (new-rating ra
                          (expected (- rb ra))
                          game)

           p2 (new-rating rb
                          (expected (- ra rb))
                          (- game)))))

(defn update-ratings
  [ratings games]
  (if (empty? games)
    ratings
    (recur (new-ratings ratings (first games))
           (rest games))))

(defn initial-rankings
  [players]
  (zipmap players (repeat initial-ranking)))

(defn- extract-players
  [games]
  (vec (set (apply concat
                   (for [[f s] games]
                     [f s])))))

(defn normalize-game
  [{:keys [p1_name p2_name p1_goals p2_goals]}]
  (cond
    (= p1_goals p2_goals) [p1_name p2_name 0.5]
    (> p1_goals p2_goals) [p1_name p2_name 1]
    (> p2_goals p1_goals) [p2_name p1_name 1]))

(defn compute-rankings
  [games]
  (update-ratings (initial-rankings (extract-players games))
                  games))
