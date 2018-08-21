(ns elo.elo
  "Main namespace containing all the core logic.
  See the mathematical details of the Elo formula used here:
  https://en.wikipedia.org/wiki/Elo_rating_system")

(def k 32)
(def initial-ranking 1500)

(defn valid-rankings?
  [rankings]
  (== 0 (mod (apply + rankings) initial-ranking)))

(defn expected
  [diff]
  (/ 1.0 (inc (Math/pow 10 (/ diff 400)))))

(defn new-rating
  [old expected game]
  (+ old (* k (- game expected))))

(defn invert-score
  [score]
  (cond (zero? score) 1
        (= 1 score) 0
        :else 1/2))

(defn new-ratings
  [ratings [p1 p2 score]]

  (let [ra (get ratings p1)
        rb (get ratings p2)]

    (assoc ratings
           p1 (new-rating ra
                          (expected (- rb ra))
                          score)

           p2 (new-rating rb
                          (expected (- ra rb))
                          (invert-score score)))))

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
  "Normalize the game identifying winner and loser (or draw) from the number of goals.
  With this approach the goal difference doesn't matter, but with
  changes to this normalizatione that could also be taken into account."

  ;;TODO: if we return both scores in one go we don't need the extra
  ;;normalizationq done above
  [{:keys [p1 p2 p1_goals p2_goals]}]
  (cond
    (= p1_goals p2_goals) [p1 p2 0.5]
    (> p1_goals p2_goals) [p1 p2 1]
    (> p2_goals p1_goals) [p1 p2 0]))

(defn compute-rankings
  ([games players]
   {:post [(valid-rankings? (vals %))]}
   (update-ratings (initial-rankings players)
                   games))
  ([games]
   (compute-rankings games (extract-players games))))
