(ns elo.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.format :as f]
            [clj-time.coerce :as tc]
            [clojure.data.csv :as csv]
            [environ.core :refer [env]]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [elo.helpers :refer [*test-db* test-db-uri]]))

(def local-db "postgres://elo@localhost:5445/elo")

(defn db-spec
  []
  (or (env :database-url)
      *test-db*))

(defn- store-sql
  [params]
  (-> (h/insert-into :game)
      (h/values [params])))

(defn conform
  [data]
  (-> data
      (update :p1_goals #(Integer. %))
      (update :p2_goals #(Integer. %))))

(defn conform-with-date
  [data]
  (-> data
      conform
      (update :played-at
              #(tc/to-sql-time (f/parse
                                (f/formatter "DD/MM/yyyy HH:mm:ss") %)))))

(defn store
  [params]
  (jdbc/execute! (db-spec)
                 (sql/format (store-sql (conform params)))))

(defn- load-games-sql
  []
  (-> (h/select :*)
      (h/from :game)))

(defn load-games
  []
  (jdbc/query (db-spec)
              (sql/format (load-games-sql))))

(defn insert-game-sql
  [values]
  (-> (h/insert-into :game)
      (h/values values)))

(defn- import-csv
  [filename]
  (let [content (csv/read-csv (slurp filename))
        strip-header (rest content)
        parsed
        (for [[played-at p1_name p2_name p1_goals p2_goals p1_team p2_team] strip-header]
          {:p1_name p1_name
           :p2_name p2_name
           :p1_goals p1_goals
           :p2_goals p2_goals
           :p1_team p1_team
           :p2_team p2_team
           :played-at played-at})]

    (jdbc/execute! local-db ;;(db-spec)
                   (sql/format (insert-game-sql
                                (map conform-with-date parsed))))))

(defn -main
  [& [filename]]
  (import-csv filename))

;; lein run -m elo.db sample.csv
