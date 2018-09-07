(ns elo.db-test
  (:require [elo.db :as sut]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(use-fixtures :each sut/wrap-test-db-call)
