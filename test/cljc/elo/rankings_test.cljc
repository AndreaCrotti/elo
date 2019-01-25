(ns elo.rankings-test
  (:require [elo.rankings :as sut]
            #?(:clj [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :as t :refer-macros [deftest testing is]])))

(def sample-game
  {:p2 "edbebe16-dd1c-414e-b9b2-4c9e38d1928e"
   :league_id "32ddffd6-d022-48ef-9720-9ff358a13798"
   :p1_using "XU"
   :p1 "fb6f256d-514b-479f-a70a-63987946de15"
   :played_at "2017-01-24T00:31:50Z"
   :id "e0b57b6a-ab45-4e67-bc9b-4e869af4ed7f"
   :p1_points 0
   :p2_points 8
   :p2_using "rH"
   :recorded_at "2019-01-20T10:03:31Z"})

(def sample-player
  {:id "fb6f256d-514b-479f-a70a-63987946de15"
   :name "John"
   :user_id "e2015c5d-1f17-4cb6-84c1-35a75220ec25"
   :league_id "32ddffd6-d022-48ef-9720-9ff358a13798"
   :player_id "fb6f256d-514b-479f-a70a-63987946de15"
   :id_2 "a05f39dc-2b7b-4aa7-95c1-bf7e79b52747"
   :active true})

#_(deftest rankings-test
  (testing "Simple rankings computation"
    (is (= []
           (sut/rankings [sample-game]
                         [sample-player]
                         1
                         #{}
                         elo.shared-config/default-game-config)))))

(deftest rankings-history-test
  (testing "Rankings history computation"
    (is (= [{:ranking 1500,
             :player "John",
             :game-idx 0,
             :time "2017-01-24T00:31:50Z",
             :result "John vs null: (0 - 8)"}]

           (sut/rankings-history [sample-player]
                                 [sample-player]
                                 [sample-game]
                                 1)))))

(deftest domain-test
  (testing "Rankings domain"
    ;; this is actually not very useful
    (is (= [1500 1500]
           (sut/domain [sample-game]
                       [sample-player])))))
