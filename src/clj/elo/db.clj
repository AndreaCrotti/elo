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
  (let [new-params
        (-> params
            (update :p1-goals #(Integer. %))
            (update :p2-goals #(Integer. %)))]

    (jdbc/execute! (db-spec)
                   (sql/format (store-sql new-params)))))

(defn- load-games-sql
  []
  (-> (h/select :*)
      (h/from :games)))

(defn load-games
  []
  (jdbc/query (db-spec)
              (sql/format (load-games-sql))))
