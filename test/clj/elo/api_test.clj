(ns elo.api-test
  (:require [elo.api :as sut]
            [ring.mock.request :as mock]
            [elo.helpers :refer [wrap-db-call]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(use-fixtures :each wrap-db-call)

(s/def ::p1-name string?)
(s/def ::p2-name string?)
(s/def ::p1-team string?)
(s/def ::p2-team string?)
(s/def ::p1-goals int?)
(s/def ::p2-goals int?)

(s/def ::game (s/keys :req-un [::p1-name
                               ::p2-name
                               ::p1-team
                               ::p2-team
                               ::p1-goals
                               ::p2-goals]))

(deftest store-results-test
  (testing "Should be able to store results"
    (let [sample {:p1-name "bob"
                  :p2-name "fred"
                  :p1-team "RM"
                  :p2-team "Juv"
                  :p1-goals 3
                  :p2-goals 0}

          request (mock/request :post "/store" sample)
          response (sut/app request)
          loaded-games (sut/app (mock/request :get "/games"))]

      (is (= {:status 201,          
              :headers {"Content-Type" "application/octet-stream"},
              :body "The result was stored correctly"}

             response))

      ;;TODO: should actually return the list of games to load
      #_(is (nil? loaded-games)))))
