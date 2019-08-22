(ns byf.seed
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [byf.generators :as gen]
            [byf.shared-config :as shared]
            [byf.db :as db]
            [taoensso.timbre :as log]))

(def default-n-games 900)
(def players-names ["John" "Charlie" "Frank" "Fitz" "Emily"])

(defn random-game
  [player-ids]
  (let [p1-id (rand-nth player-ids)
        p2-id (rand-nth (remove #(= % p1-id) player-ids))]

    (gen/game-gen {:p1 p1-id
                   :p2 p2-id
                   :p1_points (rand-nth (shared/opts :fifa :points))
                   :p2_points (rand-nth (shared/opts :fifa :points))})))

(defn get-player-ids
  [league-id]
  (map :id
       (db/query (fn [] {:select [:id]
                         :from [:league_players]
                         :where [:= :league_id league-id]}))))

(defn random-ts
  []
  (tc/to-sql-time
   (let [zero (tc/to-epoch (t/date-time 2018 1 1))
         end (tc/to-epoch (t/date-time 2019 1 1))
         length (- end zero)]

     (tc/from-epoch (+ zero (rand-int length))))))

(defn create-league!
  []
  (let [company-id (db/gen-uuid)
        league-id (db/gen-uuid)

        company {:id company-id
                 :name "Sample Company Time Fixed"}

        league {:company_id company-id
                :name "Very big league"
                :id league-id}]

    (db/add-company! company)
    (db/add-league! league)
    league-id))

(defn- add-players!
  [league-id]
  (let [players (map #(gen/player-gen {:name %}) players-names)]
    (doseq [n (range (count players))]
      (let [pl (nth players n)]
        #_(log/debug "Creating player number" n pl)
        (db/add-player-full! (assoc pl
                                    :id (:id pl)
                                    :email "sample-email"
                                    :league_id league-id))))
    (map :id players)))

(defn- add-games!
  ([league-id player-ids]
   (add-games! league-id player-ids default-n-games))

  ([league-id player-ids n-games]
   (let [games (repeatedly n-games #(random-game player-ids))
         games-full (map #(merge % {:league_id league-id
                                    :played_at (random-ts)})
                         games)]

     (doseq [game games-full]
       #_(log/debug game)
       (db/add-game! game)))))

(defn seed
  [league-id]
  (add-games! league-id (add-players! league-id)))

(defn -main
  [& args]
  (seed (create-league!)))

;; run witn `lein run -m byf.seed`
