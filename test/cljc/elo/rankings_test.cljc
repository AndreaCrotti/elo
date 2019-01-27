(ns elo.rankings-test
  (:require [elo.rankings :as sut]
            [elo.shared-config :as shared]
            [elo.generators :as gen]
            [medley.core :as medley]
            #?(:clj [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :as t :refer-macros [deftest testing is]])))

(defn- uuids->str
  [m]
  (medley/map-vals #(if (uuid? %)
                      (str %)
                      %)
                   m))

(def p1-id "fb6f256d-514b-479f-a70a-63987946de15")
(def p2-id "edbebe16-dd1c-414e-b9b2-4c9e38d1928e")

(def games
  [(uuids->str (gen/game-gen {:p1 p1-id
                              :p2 p2-id
                              :p1_points 3
                              :p2_points 0}))

   (uuids->str (gen/game-gen {:p1 p1-id :p2 p2-id
                              :p1_points 3
                              :p2_points 0}))])

(def players
  [(uuids->str (gen/player-gen {:id p1-id}))
   (uuids->str (gen/player-gen {:id p2-id}))])

(deftest rankings-test
  (testing "Simple rankings computation"
    (is (= [{:id p1-id
             :ranking 1516.0
             :ngames 1}
            {:id p2-id
             :ranking 1484.0
             :ngames 1}]

           (sut/rankings games players
                         1
                         #{}
                         shared/default-game-config)))))

(deftest rankings-history-test
  (testing "Rankings history computation"
    (is (= [1516.0 1484.0]
           (map :ranking
                (sut/rankings-history players players games 1))))))

(deftest domain-test
  (testing "Rankings domain"
    (is (= [1469.4695015289756 1530.5304984710244] (sut/domain games players)))))

(deftest last-ranking-changes-test
  (testing "Simple last ranking changes"
    (let [history (sut/rankings-history players players games 1)]
      ;;XXX: this kind of resul doesn't help much but just to have one test
      (is (= {(-> players first :name) -1516.0}
             (sut/last-ranking-changes history #{(-> players first :name)}))))))
