(ns elo.api-test
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.core.codecs :refer [bytes->str]]
            [buddy.core.codecs.base64 :as b64]
            [clojure.data.json :as json]
            [clojure.test :refer [deftest is testing use-fixtures join-fixtures]]
            [elo.api :as sut]
            [elo.db :as db]
            [elo.generators :as gen]
            [environ.core :refer [env]]
            [ring.mock.request :as mock])
  (:import (java.util UUID)))


(defn- gen-uuid [] (UUID/randomUUID))

;; this league is always present, which makes it easier to write tests using it 
(def sample-league-id (gen-uuid))
(def sample-league {:id sample-league-id :name "Sample League"})

(use-fixtures :each (join-fixtures [db/wrap-db-call (fn [test-fn]
                                                      (db/add-league! sample-league)
                                                      (test-fn))]))

(defn- make-admin-header
  []
  (format "Basic %s" (-> (b64/encode (format "%s:%s" "admin" "password"))
                         (bytes->str))))

(defn- write-api-call
  [endpoint content]
  (let [response (sut/app (mock/request :post endpoint content))]
    (assert (= 201 (:status response)))
    (json/read-str (:body response))))

(defn- store-users!
  []
  (let [p1 {:id (gen-uuid) :name "bob" :email "email"}
        p2 {:id (gen-uuid) :name "fred" :email "email"}]
    (db/add-player! p1)
    (db/add-player! p2)

    (db/add-player-to-league! {:player_id (:id p1)
                               :league_id sample-league-id})

    (db/add-player-to-league! {:player_id (:id p2)
                               :league_id sample-league-id})
    [p1 p2]))

(deftest store-results-test
  (testing "Should be able to store results"
    (let [[p1 p2] (store-users!)
          sample {:p1 (:id p1)
                  :p2 (:id p2)
                  :league_id sample-league-id
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0
                  :played_at "2018-08-16+01:0001:48:00"}

          response (write-api-call "/add-game" sample)
          games (sut/app (mock/request :get "/games" {:league_id sample-league-id}))

          desired {"p1" (str (:id p1))
                   "p1_goals" 3,
                   "p1_team" "RM",
                   "p2" (str (:id p2)),
                   "p2_goals" 0,
                   "p2_team" "Juv"}]

      (is (= [1] response))

      (is (= 200 (:status games)))

      (is (= desired
             (select-keys
              (first (json/read-str (:body games)))
              ["p1_goals" "p2_goals"
               "p1" "p2"
               "p1_team" "p2_team"]))))))

(deftest get-rankings-test
  (testing "Simple computation"
    (let [[p1 p2] (store-users!)
          other (first (gen/player-gen {:name "other"} 1))
          _ (db/add-player! other)
          _ (db/add-player-to-league! {:player_id (:id other)
                                       :league_id sample-league-id})
          sample {:p1 (:id p1)
                  :p2 (:id p2)
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0
                  :league_id sample-league-id
                  :played_at "2018-08-16+01:0001:48:00"}]

      (sut/app (mock/request :post "/add-game" sample))

      (let [rankings (sut/app (mock/request :get "/rankings"  {:league_id sample-league-id}))]
        (is (= 200 (:status rankings)))
        (is (=
             ;; should move out ngames & other information to a
             ;; different returned map instead?
             [{"id" (str (:id p1)) "ranking" 1516.0 "ngames" 1}
              {"id" (str (:id other)) "ranking" 1500 "ngames" 0}
              {"id" (str (:id p2)) "ranking" 1484.0 "ngames" 1}]
             
             (json/read-str (:body rankings))))))))

(deftest register-user-test
  (with-redefs [env (assoc env :admin-password "admin-password")]
    (testing "Add a new user without right user/password"
      (let [user {:name "name" :email "email"}
            response (sut/app (mock/request :post "/add-player" user))]

        (is (= 401 (:status response)))))

    (testing "Adds a new user with the right user/password"
      (with-redefs [authenticated? (fn [r] true)]
        (let [params {:name "name"
                      :email "email"}

              response (sut/app (mock/header
                                 (mock/request :post "/add-player" params)
                                 "Authorization" (make-admin-header)))]

          (is (= {:status 201
                  :headers {"Content-Type" "application/json"
                            "Location" "http://localhost/players"}
                  :body "[1]"}
                 response)))))))
