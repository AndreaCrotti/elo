(ns byf.api-test
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.core.codecs :refer [bytes->str]]
            [buddy.core.codecs.base64 :as b64]
            [clojure.data.json :as json]
            [clojure.test :refer [deftest is testing use-fixtures join-fixtures]]
            [byf.api :as sut]
            [byf.db :as db]
            [environ.core :refer [env]]
            [ring.mock.request :as mock])
  (:import (java.util UUID)))

(defn- gen-uuid [] (UUID/randomUUID))

;; this league is always present, which makes it easier to write tests using it
(def sample-company-id (gen-uuid))

(def sample-league-id (gen-uuid))
(def sample-league {:id sample-league-id
                    :company_id sample-company-id
                    :name "Sample League"})

(defn- setup-league-fixture
  [test-fn]
  (db/add-company! {:id sample-company-id
                    :name "Sample Company"})
  (db/add-league! sample-league)
  (test-fn))

(use-fixtures :each (join-fixtures [db/wrap-test-db-call setup-league-fixture]))

(defn- make-admin-header
  []
  (format "Basic %s" (-> (b64/encode (format "%s:%s" "admin" "password"))
                         (bytes->str))))

(defn read-api-call
  ([endpoint]
   (read-api-call endpoint {}))

  ([endpoint args]
   (-> (mock/request :get endpoint args)
       sut/app)))

(defn- write-api-call
  [endpoint content]
  (let [full-api-path (str "/api" endpoint)
        {:keys [status] :as response}
        (-> (mock/request :post full-api-path content)
            sut/app)]

    (when-not (contains? #{201 401} status)
      (println "bad response, got " response)
      (throw (Exception. "bad response, should be #{201, 401}")))
    response))

(defn- store-users!
  []
  (let [p1 {:name "bob" :email "email" :league_id sample-league-id}
        p2 {:name "fred" :email "email" :league_id sample-league-id}
        p1-id (db/add-player-full! p1)
        p2-id (db/add-player-full! p2)]

    [p1-id p2-id]))

(deftest store-results-test
  (testing "Should be able to store results"
    (let [[p1-id p2-id] (store-users!)
          sample {:p1 (:player-id p1-id)
                  :p2 (:player-id p2-id)
                  :league_id sample-league-id
                  :p1_using "RM"
                  :p2_using "Juv"
                  :p1_points 3
                  :p2_points 0
                  :played_at  "2018-08-29+01:0021:50:32"}

          _ (write-api-call "/add-game" sample)
          games (read-api-call "/api/games" {:league_id sample-league-id})

          desired {"p1" (str (:player-id p1-id))
                   "p1_points" 3,
                   "p1_using" "RM",
                   "p2" (str (:player-id p2-id)),
                   "p2_points" 0,
                   "p2_using" "Juv"
                   "played_at" "2018-08-29 21:50:00.32"}]

      (is (= 200 (:status games)))

      #_(is (= desired
               (select-keys
                (first (json/read-str (:body games)))
                ["p1_points" "p2_points"
                 "p1" "p2"
                 "p1_using" "p2_using"
                 "played_at"]))))))

(deftest get-players-test
  (testing "Fetching all the existing players"
    (db/add-player-full! {:name "john"
                          :email "mail"
                          :league_id sample-league-id})

    (let [response (read-api-call "/api/players" {:league_id sample-league-id})
          body-obj (json/read-str (:body response))]
      (is (= 200 (:status response)))
      (is (= 1 (count body-obj)))
      (is (= {"name" "john",
              "active" true}
             (-> body-obj
                 first
                 (dissoc "id")))))))

(deftest add-player-user-test
  (with-redefs [env (assoc env :admin-password "admin-password")]
    (testing "Add a new user without right user/password"
      (let [user {:name "name" :email "email" :league_id sample-league-id}
            response (write-api-call "/add-player" user)]

        (is (= 401 (:status response)))
        (is (empty? (db/load-players sample-league-id)))))

    (testing "Adds a new user with the right user/password"
      (with-redefs [authenticated? (fn [r] true)]
        (let [params {:name "name" :email "email" :league_id sample-league-id}
              ;;TODO: use the write helper also here
              response (sut/app (mock/header (mock/request :post "/api/add-player" params)
                                 "Authorization" (make-admin-header)))]

          (is (= 201 (:status response))))))))

(deftest get-league-test
  (testing "Get a league by the id"
    (let [response (read-api-call "/api/league" {:league_id sample-league-id})]

      (is (= 200 (:status response)))
      (is (= (str sample-league-id)
             (-> (:body response)
                 json/read-str
                 (get "id")))))))

(deftest enable-player-test
  (testing "Enabling a player or disabling it"
    (let [[p1-id _] (store-users!)
          _response     (write-api-call "/toggle-player" {:league_id sample-league-id
                                                          :player_id (:player-id p1-id)
                                                          :active    false})

          with-disabled (read-api-call "/api/players" {:league_id sample-league-id})
          with-disabled-first-user (-> with-disabled
                                       :body
                                       json/read-str
                                       first)]
      ;; now fetch the players
      (is (= 200 (:status with-disabled)))
      (is (false? (get with-disabled-first-user "active"))))))
