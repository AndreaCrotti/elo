(ns elo.api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [elo.api :as sut]
            [elo.db :refer [wrap-db-call]]
            [ring.mock.request :as mock]))

(use-fixtures :each wrap-db-call)

(deftest store-results-test
  (testing "Should be able to store results"
    (let [sample {:p1_name "bob"
                  :p2_name "fred"
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0}

          response (sut/app (mock/request :post "/store" sample))
          games (sut/app (mock/request :get "/games"))]

      (is (= {:status 201,
              :headers {"Content-Type" "application/json"},
              :body "[1]"}

             response))

      (is (= 200 (:status games))))))

(deftest get-rankings-test
  (testing "Simple computation"
    (let [sample {:p1_name "bob"
                  :p2_name "fred"
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0}]

      (sut/app (mock/request :post "/store" sample))

      (let [rankings (sut/app (mock/request :get "/rankings"))]
        (is (= 200 (:status rankings)))))))
