(ns data-migrations.migrate-users
  (:require [elo.db :as db]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [clojure.java.jdbc :as jdbc]
            [elo.generators :as gen])

  (:import (java.util UUID)))

(defn all-players-by-league
  []
  (let [league-ids (map :id (db/load-leagues))]
    (zipmap league-ids (map #(db/load-players %) league-ids))))

(defn update-player-user-sql
  [user-id email]
  (-> (h/update :player)
      (h/sset {:user_id user-id})
      (h/where [:= :email email])))

(defn migrate
  [company-id]
  (jdbc/with-db-transaction [tx (db/db-spec)]
    (doseq [[league-id players] (all-players-by-league)]
      (doseq [pl players]
        (let [user (gen/user-gen {:email (:email pl)
                                  :oauth2_token nil})]
          (db/add-user! user)
          (db/add-user-to-company! {:user_id (:id user)
                                    :company_id company-id})

          (jdbc/execute! tx
                         (sql/format (update-player-user-sql (:id user) (:email pl)))))))))


(defn -main [& [company-id]]
  (migrate (UUID/fromString company-id)))
