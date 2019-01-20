(ns elo.seed-test
  (:require [elo.seed :as sut]
            [honeysql.core :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [elo.db :as db]))

(use-fixtures :each db/wrap-test-db-call)

(defn- count-table
  [t]
  (:count
   (first
    (db/wrap-db-call
     (jdbc/query (db/db-spec)
                 (sql/format (db/count-sql t)))))))
(deftest seed-test
  (testing "Seeding should work with no errors on write"

    ;;TODO: just check that this works correctly and some rows get written to the db
    (sut/seed (sut/create-league!))
    (let [games-count (count-table :game)
          p-count (count-table :player)]

      (is (= 5 p-count))
      (is (= sut/n-games games-count)))))
