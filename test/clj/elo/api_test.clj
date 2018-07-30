(ns elo.api-test
  (:require [elo.api :as sut]
            [ring.mock.request :as mock]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]))

(s/def ::winner string?)
(s/def ::loser string?)
(s/def ::winning_team string?)
(s/def ::losing_team string?)
(s/def ::winning_goals int?)
(s/def ::losing_goals int?)

(s/def ::game (s/keys :req-un [::winner
                               ::loser
                               ::winning_team
                               ::losing_team
                               ::winning_goals
                               ::losing_goals]))

(deftest store-results-test
  (testing "Should be able to store results"
    (let [sample {:winner "bob"
                  :loser "fred"
                  :winning_team "RM"
                  :losing_team "Juv"
                  :winning_goals 3
                  :losing_goals 0}

          request (mock/request :post "/store" sample)
          response (sut/app request)]

      (is (= {:status 201 :body ""} response)))))
