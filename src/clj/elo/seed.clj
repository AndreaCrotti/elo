(ns elo.seed
  (:require [elo.generators :as gen]
            [elo.shared-config :as shared]
            [elo.db :as db]))

(def n-players 5)
(def n-games 40)

(defn users-players
  [n]
  (let [users (repeatedly n gen/user-gen)
        players (repeatedly n gen/player-gen)
        merged-players (map #(assoc (first %) :user_id (-> % second :id))
                            (zipmap players users))]
    [users merged-players]))

(defn random-game
  [player-ids]
  (let [p1-id (rand-nth player-ids)
        p2-id (rand-nth (remove #(= % p1-id) player-ids))]

    (gen/game-gen {:p1 (str p1-id)
                   :p2 (str p2-id)
                   :p1_points (rand-nth (shared/opts :fifa :points))
                   :p2_points (rand-nth (shared/opts :fifa :points))})))

;;TODO: now generate some random players and some random games
(defn seed
  []
  (let [company-id (db/gen-uuid)
        league-id (db/gen-uuid)

        company {:id company-id
                 :name "Sample Company"}

        league {:company_id company-id
                :name "Sample League"
                :id league-id}]

    (db/add-company! company)
    (db/add-league! league)

    (let [[users players] (users-players n-players)
          player-ids (map :id players)
          games (repeatedly n-games #(random-game player-ids))
          games-full (map #(merge % {:league_id (str league-id) :played_at "2018-08-16+01:0001:48:00"})
                          games)]

      (doseq [n (range n-players)]
        (println "Creating player number" n)
        (db/add-user! (nth users n))
        (db/add-player! (assoc (nth players n)
                               :league_id (str league-id)
                               :name (str "Player-" n))))

      (doseq [game games-full]
        (println game)
        (db/add-game! game)))))

(defn -main
  [& args]
  (seed))

;; run witn `lein run -m elo.seed`
