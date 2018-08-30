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
                :id league-id}]

    (db/add-company! company)
    (db/add-league! league)
    ))

(defn -main
  [& args]
  (seed))
