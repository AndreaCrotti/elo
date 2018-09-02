(ns elo.seed-test
  (:require [elo.seed :as sut]
            [clojure.test :refer [deftest testing is use-fixtures]]
            [elo.db :as db]))

(use-fixtures :each db/wrap-db-call)

(deftest seed-test
  (testing "Seeding should work with no errors on write"

    ;;TODO: just check that this works correctly and some rows get written to the db
    #_(sut/seed)))
