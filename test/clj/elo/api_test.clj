(ns elo.api-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.data.json :as json]
            [buddy.auth :refer [authenticated?]]
            [buddy.core.codecs.base64 :as b64]
            [buddy.core.codecs :refer [bytes->str]]
            [elo.api :as sut]
            [elo.db :refer [wrap-db-call register!]]
            [ring.mock.request :as mock])
  (:import (java.util UUID)))

(use-fixtures :each wrap-db-call)

(defn- gen-uuid [] (UUID/randomUUID))

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
    (register! p1)
    (register! p2)
    [p1 p2]))

(deftest store-results-test
  (testing "Should be able to store results"
    (let [[p1 p2] (store-users!)
          sample {:p1 (:id p1)
                  :p2 (:id p2)
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0}

          response (write-api-call "/store" sample)
          games (sut/app (mock/request :get "/games"))

          desired {"p1" (str (:id p1))
                   "p1_goals" 3,
                   "p1_team" "RM",
                   "p2" (str (:id p2)),
                   "p2_goals" 0,
                   "p2_team" "Juv",}]

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
          other {:id (gen-uuid) :name "other" :email "otheremail"}
          _ (register! other)
          sample {:p1 (:id p1)
                  :p2 (:id p2)
                  :p1_team "RM"
                  :p2_team "Juv"
                  :p1_goals 3
                  :p2_goals 0}]

      (sut/app (mock/request :post "/store" sample))

      (let [rankings (sut/app (mock/request :get "/rankings"))]
        (is (= 200 (:status rankings)))
        (is (=
             [{"id" (str (:id p1)) "ranking" 1516.0 "ngames" 0},
              {"id" (str (:id p2)) "ranking" 1452.0 "ngames" 0}
              {"id" (str (:id other)) "ranking" 1500 "ngames" 0}]
             (json/read-str (:body rankings))))))))

(deftest register-user-test
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

        (is (= {:status 200, :headers {"Content-Type" "application/json"}, :body "[1]"}
               response))))))
