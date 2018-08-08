(ns elo.api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [elo.api :as sut]
            [elo.db :refer [wrap-db-call register!]]
            [ring.mock.request :as mock])
  (:import (java.util.UUID)))

(use-fixtures :each wrap-db-call)

(deftest store-results-test
  (testing "Should be able to store results"
    (let [p1 {:id 1 :name "bob" :email "email"}
          p2 {:id 2 :name "fred" :email "email"}
          sample {:p1 1
                  :p2 2
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0}

          _ (register! p1)
          _ (register! p2)
          response (sut/app (mock/request :post "/store" sample))
          games (sut/app (mock/request :get "/games"))]

      (is (= {:status 201,
              :headers {"Content-Type" "application/json"},
              :body "[1]"}

             response))

      (is (= 200 (:status games))))))

(deftest get-rankings-test
  (testing "Simple computation"
    (let [p1 {:id 1 :name "bob" :email "email"}
          p2 {:id 2 :name "fred" :email "email"}
          sample {:p1 1
                  :p2 2
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0}

          _ (register! p1)
          _ (register! p2)]

      (sut/app (mock/request :post "/store" sample))

      (let [rankings (sut/app (mock/request :get "/rankings"))]
        (is (= 200 (:status rankings)))))))
