(ns byf.db
  (:require [clojure.java.jdbc :as jdbc]
            [byf.config :refer [value]]
            [environ.core :refer [env]]
            [honeysql-postgres.helpers :as ph]
            [honeysql.core :as sql]
            [honeysql.helpers :as h])

  (:import (java.util UUID)))

(def test-db "postgres://byf@localhost:5445/byf_test")

(defn db-spec
  []
  (value :database-url))

(defmacro wrap-db-call
  [callback]
  `(try
     ~callback
     (catch java.sql.BatchUpdateException e#
       (throw (ex-info (str "BatchUpdateException: " (.getMessage (.getNextException e#)))
                       {:cause e#
                        :get-next-exception (.getNextException e#)})))))

(defn wrap-test-db-call
  [test-fn]
  (jdbc/with-db-transaction [tx
                             (or (env :database-url) test-db)
                             {:isolation :repeatable-read}]
    (jdbc/db-set-rollback-only! tx)
    (with-redefs [db-spec (fn [] tx)]
      (test-fn))))

(defn- load-games-sql
  [league-id]
  (-> (h/select :*)
      (h/from :game)
      (h/where [:= :league_id league-id])
      (h/order-by [:played_at :asc]
                  [:recorded_at :asc])))

(defn load-players-sql
  [league-id]
  (-> (h/select :*)
      (h/from [:player :pl])
      (h/join [:league_players :lg]
              [:= :pl.id :lg.player_id])

      (h/where [:= :lg.league_id league-id])))

(defn load-leagues-sql
  []
  (-> (h/select :*)
      (h/from :league)
      (h/where [:= :enabled true])))

(defn load-companies-sql
  []
  (-> (h/select :*)
      (h/from :company)))

(defn load-league-sql
  [league-id]
  (-> (h/select :*)
      (h/from :league)
      (h/where [:= :id league-id])))

(defn query
  [func & args]
  (jdbc/query (db-spec)
              (sql/format (apply func args))))

(defn get-single
  [func & args]
  (first (apply query func args)))

(defn load-games [league-id] (query load-games-sql league-id))

(defn load-players [league-id] (query load-players-sql league-id))

(defn load-leagues [] (query load-leagues-sql))

(defn load-league [league-id] (get-single load-league-sql league-id))

(defn load-companies [] (query load-companies-sql))

(defn- store-sql
  [params]
  (-> (h/insert-into :game)
      (h/values [params])))

(defn gen-uuid [] (UUID/randomUUID))

(defn add-row-sql
  [table]
  (fn [params]
    (-> (h/insert-into table)
        (h/values [params])
        (ph/returning :id))))

(def add-player-sql (add-row-sql :player))

(def add-league-sql (add-row-sql :league))

(def add-player-to-league-sql (add-row-sql :league_players))

(def add-user-to-company-sql (add-row-sql :company_users))

(def add-company-sql (add-row-sql :company))

(defn- add-row!
  [sql-func]
  (fn [params]
    (->
     (jdbc/execute! (db-spec)
                    (sql/format (sql-func params))
                    {:return-keys [:id]})
     :id)))

(def add-league! (add-row! add-league-sql))

(def add-player-to-league! (add-row! add-player-to-league-sql))

(def add-user-to-company! (add-row! add-user-to-company-sql))

(def add-company! (add-row! add-company-sql))

(defn add-game!
  [params]
  {:pre [(not= (:p1 params) (:p2 params))]}
  ((add-row! store-sql) params))

(def add-player! (add-row! add-player-sql))

(defn add-player-full!
  [{:keys [name email league_id id]}]
  ;;TODO: wrap it in a db transaction and remove the NULL constraint
  ;;from the player table!
  (let [company-id (:company_id (load-league league_id))
        player-id (add-player! {name name :id (or id (gen-uuid))})]

    (add-player-to-league! {:league_id league_id :player_id player-id})))

(defn count-sql [table]
  (-> (h/select :%count.*)
      (h/from table)))

(defn player-name
  [player-id]
  (-> (h/select :name)
      (h/from :player)
      (h/where [:= :id player-id])))
