(ns data-migrations.migrate-users
  (:require [elo.db :as db]
            [elo.generators :as gen]))

;; should this depend on the `elo` namespace at all??  if it does then
;; it's hard to make these migrations in sync, even if it might not be
;; so important

;; Steps required for this migration:
;; - create lots of empty users (as many as the players)
;; - add these users to a certain company
;; - link together these users with existing users copying over the email

(defn all-players-by-league
  []
  (let [league-ids (map :id (db/load-leagues))]
    (zipmap league-ids (map #(db/load-players %) league-ids))))

(defn migrate
  [company-id]
  (doseq [[league-id players] (all-players-by-league)]
    (doseq [pl players]
      (let [user (gen/user-gen {:email (:email pl)})]
        (db/add-user! user)
        (db/add-user-to-company! (:id user) company-id)
        ))))
