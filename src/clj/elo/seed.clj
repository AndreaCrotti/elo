(ns elo.seed
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [elo.generators :as gen]
            [elo.shared-config :as shared]
            [elo.db :as db]))

(def n-players 5)
(def n-games 40)

(defn random-game
  [player-ids]
  (let [p1-id (rand-nth player-ids)
        p2-id (rand-nth (remove #(= % p1-id) player-ids))]

    (gen/game-gen {:p1 p1-id
                   :p2 p2-id
                   :p1_points (rand-nth (shared/opts :fifa :points))
                   :p2_points (rand-nth (shared/opts :fifa :points))})))

(defn get-player-ids
  []
  (map :id
       (db/query (fn [] {:select [:id]
                         :from [:player]}))))

(defn random-ts
  []
  (tc/to-sql-time
   (let [zero (tc/to-epoch (t/date-time 2018 1 1))
         end (tc/to-epoch (t/date-time 2020 1 1))
         length (- end zero)]

     (tc/from-epoch (+ zero (rand-int length))))))

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

    (let [players (repeatedly n-players gen/player-gen)]

      (doseq [n (range n-players)]
        (println "Creating player number" n (nth players n))
        (db/add-player-full! (assoc (nth players n)
                                    :email "sample-email"
                                    :league_id league-id)))

      (let [games (repeatedly n-games #(random-game (get-player-ids)))
            games-full (map #(merge % {:league_id league-id
                                       :played_at (random-ts)})
                            games)]

        (doseq [game games-full]
          (println game)
          (db/add-game! game))))))

(defn -main
  [& args]
  (seed))

;; run witn `lein run -m elo.seed`
