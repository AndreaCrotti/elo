(ns elo.seed
  (:require [elo.generators :as gen]
            [elo.db :as db]))

(def n-players 5)
(def n-games 40)

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
        (println "Creating player number" n)
        (db/add-player! (assoc (nth players n)
                               :league_id (str league-id)
                               :name (str "Player-" n))))

      (let [games (gen/game-gen {:p1 (-> players first :id str)
                                 :p2 (-> players second :id str)
                                 :p1_goals "1"
                                 :p2_goals "2"
                                 :league_id (str league-id)
                                 :played_at "2018-08-16+01:0001:48:00"}
                                n-games)]

        (doseq [game games]
          (println game)
          (db/add-game! game))))))

(defn -main
  [& args]
  (seed))
