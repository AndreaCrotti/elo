(ns elo.db
  (:require [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [elo.helpers :refer [*test-db* test-db-uri]]))

(defn db-spec
  []
  (or (env :database-url)
      *test-db*))

(defn- store-sql
  [params]
  (-> (h/insert-into :game)
      (h/values [params])))

(defn store
  [params]
  (jdbc/execute! (db-spec)
                 (sql/format (store-sql params))))

(defn load-games
  []
  (-> (h/select :*)
      (h/from :games)))
