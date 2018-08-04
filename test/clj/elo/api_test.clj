(ns elo.api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [elo.api :as sut]
            [elo.db :as db]
            [elo.helpers :refer [wrap-db-call]]
            [ring.mock.request :as mock]))

(use-fixtures :each wrap-db-call)

(deftest store-results-test
  (testing "Should be able to store results"
    (let [sample {:p1-name "bob"
                  :p2-name "fred"
                  :p1-team "RM"
                  :p2-team "Juv"
                  :p1-goals 3
                  :p2-goals 0}

          response (sut/app (mock/request :post "/store" sample))
          games (sut/app (mock/request :get "/games"))
          ]

      (is (= {:status 201,
              :headers {"Content-Type" "application/octet-stream"},
              :body "The result was stored correctly"}

             response))

      (is (= 200 (:status games))))))

(deftest get-rankings-test
  (testing "Simple computation"
    (let [sample {:p1-name "bob"
                  :p2-name "fred"
                  :p1-team "RM"
                  :p2-team "Juv"
                  :p1-goals 3
                  :p2-goals 0}

          response (sut/app (mock/request :post "/store" sample))]

      #_(sut/app (mock/request :post "/store" sample))

      #_(let [rankings (sut/app (mock/request :get "/rankings"))]
        (is (= {} rankings))))))
