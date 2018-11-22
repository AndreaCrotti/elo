(ns elo.seed
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [elo.generators :as gen]
            [elo.shared-config :as shared]
            [elo.db :as db]))

(def n-games 100)
(def players ["John" "Charlie" "Frank" "Fitz" "Emily"])

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
   (let [zero (tc/to-epoch (t/date-time 2017 1 1))
         end (tc/to-epoch (t/date-time 2018 10 1))
         length (- end zero)]

     (tc/from-epoch (+ zero (rand-int length))))))

(defn seed
  []
  (let [company-id (db/gen-uuid)
        league-id (db/gen-uuid)

        company {:id company-id
                 :name "Sample Company Time Fixed"}

        league {:company_id company-id
                :name "Sample League Time Fixed"
                :id league-id}]

    (db/add-company! company)
    (db/add-league! league)

    (let [players (map #(gen/player-gen {:name %}) players)]

      (doseq [n (range (count players))]
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
