(ns elo.algorithms.elo
  "Implementation of the ELO algorithm")

;; these are the parameters that can be changed at runtime
;; so that they can be set by the client directly
(def default-config
  { ;; valid range from 20 to 44
   :k 32
   ;; valid range from 100 to 2000
   ;; should also move the k accordingly
   :initial-ranking 1500
   ;; should be a percent from 0 to 1
   :points-count 0})

(def k 32)
(def initial-ranking 1500)

(defn valid-rankings?
  [rankings]
  (== 0 (mod (apply + rankings) initial-ranking)))

(defn expected
  [diff]
  (/ 1.0 (inc (Math/pow 10 (/ diff 400)))))

(defn new-rating
  [old expected score {:keys [k] :as config}]
  (+ old (* k (- score expected))))

(defn invert-score
  [score]
  (cond (zero? score) 1
        (= 1 score) 0
        :else score))

(defn new-rankings
  ([rankings result]
   (new-rankings rankings result default-config))

  ([rankings [p1 p2 score] config]
   (let [ra (get rankings p1)
         rb (get rankings p2)]

     (assoc rankings
            p1 (new-rating ra
                           (expected (- rb ra))
                           score
                           config)

            p2 (new-rating rb
                           (expected (- ra rb))
                           (invert-score score)
                           config)))))

;;TODO: this should return the whole history of the rankings instead
;;of simply the last one??
(defn update-rankings
  [rankings games config]
  (if (empty? games)
    rankings
    (recur (new-rankings rankings (first games) config)
           (rest games)
           config)))

(defn initial-rankings
  ([players]
   (initial-rankings players default-config))

  ([players {:keys [initial-ranking] :as config}]
   (zipmap players (repeat initial-ranking))))

(defn extract-players
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
  [{:keys [p1 p2 p1_points p2_points]}]
  (cond
    (= p1_points p2_points) [p1 p2 0.5]
    (> p1_points p2_points) [p1 p2 1]
    (> p2_points p1_points) [p1 p2 0]))

(defn compute-rankings
  ([games players]
   ;; this could simply have a bit more leeway to work (2/3% max)
   #_{:post [(valid-rankings? (vals %))]}
   (compute-rankings games players default-config))

  ([games players config]
   ;; this could simply have a bit more leeway to work (2/3% max)
   #_{:post [(valid-rankings? (vals %))]}
   (update-rankings (initial-rankings players config)
                    games
                    (merge default-config config))))
