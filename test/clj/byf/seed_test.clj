(ns byf.seed-test
  (:require [byf.seed :as sut]
            [honeysql.core :as sql]
            [clojure.java.jdbc :as jdbc]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [byf.db :as db]))

(defn- count-table
  [t]
  (:count
   (first
    (db/wrap-db-call
     (jdbc/query (db/db-spec)
                 (sql/format (db/count-sql t)))))))

(deftest seed-test
  (testing "Seeding should work with no errors on write"
    (db/with-rollback
      (sut/seed (sut/create-league!))
      (let [games-count (count-table :game)]
        (is (= sut/n-games games-count))))))
