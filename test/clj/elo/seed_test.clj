(ns elo.seed-test
  (:require [elo.seed :as sut]
            [honeysql.core :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [elo.db :as db]))

(use-fixtures :each db/wrap-test-db-call)

(deftest seed-test
  (testing "Seeding should work with no errors on write"

    ;;TODO: just check that this works correctly and some rows get written to the db
    (sut/seed)

    (let [games-count
          (db/wrap-db-call
           (jdbc/query (db/db-spec)
                       (sql/format (db/count-sql :game))))]

      (is (= [{:count 40}] games-count)))))
