(ns elo.rankings-test
  (:require [elo.rankings :as sut]
            [elo.shared-config :as shared]
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

(def sample-player-2
  {:id "edbebe16-dd1c-414e-b9b2-4c9e38d1928e"
   :name "Emily"
   :user_id "e2015c5d-1f17-4cb6-84c1-35a75220ec25"
   :league_id "32ddffd6-d022-48ef-9720-9ff358a13798"
   :player_id "fb6f256d-514b-479f-a70a-63987946de15"
   :id_2 "a05f39dc-2b7b-4aa7-95c1-bf7e79b52747"
   :active true})

(def players [sample-player sample-player-2])
(def games [sample-game])

(deftest rankings-test
  (testing "Simple rankings computation"
    (is (= [{:id "edbebe16-dd1c-414e-b9b2-4c9e38d1928e"
             :ranking 1516.0}
            {:id "fb6f256d-514b-479f-a70a-63987946de15"
             :ranking 1484.0}]

           (sut/rankings games players
                         1
                         #{}
                         shared/default-game-config)))))

(deftest rankings-history-test
  (testing "Rankings history computation"
    (is (= [{:ranking 1516.0
             :player "Emily"
             :game-idx 0
             :time "2017-01-24T00:31:50Z"
             :result "John vs Emily: (0 - 8)"}
            {:ranking 1484.0
             :player "John"
             :game-idx 0
             :time "2017-01-24T00:31:50Z"
             :result "John vs Emily: (0 - 8)"}]

           (sut/rankings-history players players games 1)))))

(deftest domain-test
  (testing "Rankings domain"
    (is (= [1484.0 1516.0] (sut/domain games players)))))

(deftest last-ranking-changes-test
  (testing "Simple last ranking changes"
    (let [history (sut/rankings-history players players games 1)]
      ;;XXX: this kind of resul doesn't help much but just to have one test
      (is (= {"John" -1484.0}
             (sut/last-ranking-changes history #{(:name sample-player)}))))))
