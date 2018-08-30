(ns elo.seed
  (:require [elo.generators :as gen]
            [elo.db :as db]))

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
    (let [players (gen/player-gen {} 5)]
      (doseq [n (range 5)]
        (db/add-player! (assoc (nth players n)
                               :league_id (str league-id)
                               :name (str "Player-" n)))))))

(defn -main
  [& args]
  (seed))
