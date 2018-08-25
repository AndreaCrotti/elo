(ns elo.db
  (:require [clj-time.coerce :as tc]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clojure.data.csv :as csv]
            [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [honeysql.core :as sql]
            [honeysql.helpers :as h])

  (:import (java.util UUID)))

(def local-db "postgres://elo@localhost:5445/elo")
(def test-db "postgres://elo@localhost:5445/elo_test")
(def google-sheet-timestamp-format "dd/MM/yyyy HH:mm:ss")

(defn db-spec
  []
  (or (env :database-url) local-db))

(defn wrap-db-call
  [test-fn]
  (jdbc/with-db-transaction [tx
                             (or (env :database-url) test-db)
                             {:isolation :repeatable-read}]
    (jdbc/db-set-rollback-only! tx)
    (with-redefs [db-spec (fn [] tx)]
      (test-fn))))

(defn- store-sql
  [params]
  (-> (h/insert-into :game)
      (h/values [params])))

(defn to-uuid
  [uuid-str]
  (UUID/fromString uuid-str))

(def transformations
  {:p1 to-uuid
   :p2 to-uuid
   :league_id to-uuid
   :p1_goals #(Integer. %)
   :p2_goals #(Integer. %)})

(defn conform
  [data]
  (-> data
      (assoc :id (UUID/randomUUID))
      (update :p1 #(UUID/fromString %))
      (update :p2 #(UUID/fromString %))
      (update :league_id #(UUID/fromString %))
      (update :p1_goals #(Integer. %))
      (update :p2_goals #(Integer. %))))

(defn conform-with-date
  [data]
  (-> data
      conform
      (update :played-at
              #(tc/to-sql-time (f/parse
                                (f/formatter google-sheet-timestamp-format) %)))
      (update :recorded-at
              #(tc/to-sql-time (f/parse
                                (f/formatter google-sheet-timestamp-format) %)))))


(defn add-game!
  [params]
  (let [new-params (-> params
                       conform
                       (update :played_at #(tc/to-sql-time (f/parse
                                                            (f/formatter "YYYY-MM-DDZhh:mm:SS") %)))
                       (assoc :recorded_at (tc/to-sql-time (t/now))))

        query (store-sql new-params)]

    (jdbc/execute! (db-spec)
                   (sql/format query))))

(defn register-sql
  [params]
  (-> (h/insert-into :player)
      (h/values [params])))

(defn- add-row!
  [sql-func]
  (fn [params]
    (jdbc/execute! (db-spec)
                   (sql/format (sql-func params)))))

(defn add-league-sql
  [params]
  (-> (h/insert-into :league)
      (h/values [params])))

(defn add-company-sql
  [params]
  (-> (h/insert-into :company)
      (h/values [params])))

(defn add-player-to-league-sql
  [params]
  (-> (h/insert-into :league_players)
      (h/values [params])))

(def add-player! (add-row! register-sql))

(def add-league! (add-row! add-league-sql))

(def add-player-to-league! (add-row! add-player-to-league-sql))

(def add-company! (add-row! add-company-sql))

(defn- load-games-sql
  [league-id]
  (-> (h/select :*)
      (h/from :game)
      (h/where [:= :league_id league-id])
      (h/order-by [:played_at :desc])))

(defn load-players-sql
  [league-id]
  (-> (h/select :*)
      (h/from [:player :pl])
      (h/join [:league_players :lg]
              [:= :pl.id :lg.player_id])

      (h/where [:= :lg.league_id league-id])))

(defn- query
  [func & args]
  (jdbc/query (db-spec)
              (sql/format (apply func args))))

(defn load-games [league-id] (query load-games-sql league-id))
(defn load-players [league-id] (query load-players-sql league-id))

(defn insert-game-sql
  [values]
  (-> (h/insert-into :game)
      (h/values values)))

(defn- import-csv
  [filename names-mapping-file]
  (let [mapped-names (-> names-mapping-file slurp edn/read-string)
        content (-> filename slurp csv/read-csv)
        strip-header (rest content)
        parsed
        (for [[played-at p1_name p2_name p1_goals p2_goals _ p1_team p2_team] strip-header]
          {:p1 (get mapped-names p1_name)
           :p2 (get mapped-names p2_name)
           :p1_goals p1_goals
           :p2_goals p2_goals
           :p1_team p2_team
           :p2_team p1_team
           :played-at played-at
           :recorded-at played-at})]

    (doseq [p (map conform-with-date parsed)]
      (println p))

    (jdbc/execute! (db-spec)
                   (sql/format (insert-game-sql
                                (map conform-with-date parsed))))))

(defn -main
  [& [filename names-mapping-file]]
  (import-csv filename names-mapping-file))

;; lein run -m elo.db sample.csv prods_ids.edn
