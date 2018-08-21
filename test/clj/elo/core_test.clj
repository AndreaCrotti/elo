(ns elo.core-test
  (:require [clojure.test :refer [deftest is are testing]]
            [elo.core :as sut]))

(def games
  [[:a :b 1]
   [:b :c 0.5]
   [:a :c 0]])

(def initial-ratings
  (sut/initial-rankings [:a :b :c]))

(deftest new-rating-test
  (testing "Should compute the new rating correctly"
    (are [d exp] (= exp (sut/expected d))
      10 0.48561281583400134
      (- 10) 0.5143871841659987))
  )

(deftest elo-rating-test
  (testing "Should be a zero sum game"
    (let [game [:a :b 1]
          rs (sut/new-ratings {:a 1500 :b 1500} game)]
      (is (== 3000
              (apply + (vals rs))))))

  (testing "Order does not matter"
    (let [game [:a :b 1]
          game-inv [:b :a 0]]

      (is (= {:a 1516.0, :b 1484.0, :c 1500} 
             (sut/new-ratings initial-ratings game)
             (sut/new-ratings initial-ratings game-inv))))))

(deftest compute-rankings-test
  (testing "Passing in all the games computes in the right order returns the rankings"
    (is (= {:c 1516.0338330211207, :b 1484.736306793522, :a 1499.2298601853572}
           (sut/compute-rankings games))))

  (testing "passing new players sets them up with an initial ranking"
    (is (= {:a 1499.2298601853572,          
            :b 1484.736306793522,
            :c 1516.0338330211207,
            :d 1500}
           (sut/compute-rankings games [:a :b :c :d])))))

(deftest normalize-game-test
  (testing "Shoul compute correctly"
    (are [out game] (= out (sut/normalize-game game))
      ["Me" "You" 1] {:p1 "Me" :p2 "You" :p1_goals 2 :p2_goals 1}
      ["You" "Me" 0.5] {:p1 "You" :p2 "Me" :p1_goals 1 :p2_goals 1})))
