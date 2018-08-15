(ns elo.db
  (:require [clj-time.coerce :as tc]
            [clj-time.format :as f]
            [clojure.data.csv :as csv]
            [clojure.edn :as edn]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [honeysql.core :as sql]
            [honeysql.helpers :as h])

  (:import (java.util UUID)))

(def local-db "postgres://elo@localhost:5445/elo")
(def test-db "postgres://elo@localhost:5445/elo_test")
(def google-sheet-timestamp-format "DD/MM/yyyy HH:mm:ss")

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

(defn conform
  [data]
  (-> data
      (assoc :id (UUID/randomUUID))
      (update :p1 #(UUID/fromString %))
      (update :p2 #(UUID/fromString %))
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

(defn store!
  [params]
  (jdbc/execute! (db-spec)
                 (sql/format (store-sql (conform params)))))

(defn register-sql
  [params]
  (-> (h/insert-into :player)
      (h/values [params])))

(defn register!
  [params]
  (jdbc/execute! (db-spec)
                 (sql/format (register-sql params))))

(defn- load-games-sql
  []
  (-> (h/select :*)
      (h/from :game)
      (h/order-by :played_at)))

(defn load-players-sql
  []
  (-> (h/select :*)
      (h/from :player)))

(defn- query
  [func]
  (jdbc/query (db-spec)
              (sql/format (func))))

(defn load-games [] (query load-games-sql))
(defn load-players [] (query load-players-sql))

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

    (jdbc/execute! local-db ;;(db-spec)
                   (sql/format (insert-game-sql
                                (map conform-with-date parsed))))))

(defn -main
  [& [filename names-mapping-file]]
  (import-csv filename names-mapping-file))

;; lein run -m elo.db sample.csv
