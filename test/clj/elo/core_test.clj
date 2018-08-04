(ns elo.core-test
  (:require [clojure.test :refer [deftest is are testing]]
            [elo.core :as sut]))

(def games
  [[:a :b 1]
   [:b :c 0.5]
   [:a :c (- 1)]])

(def initial-ratings
  (sut/initial-rankings [:a :b :c]))

(deftest elo-rating-test
  (testing "Should compute new ratings correctly"
    (is (= {:a 1465.704315549496, :b 1454.1965245402773, :c 1484.0991599102267}
           (sut/update-ratings initial-ratings games))))

  (testing "Order does not matter"
    (let [game [:a :b 1]
          game-inv [:b :a (- 1)]]

      (is (= {:a 1516.0, :b 1452.0, :c 1500}
             (sut/new-ratings initial-ratings game)
             (sut/new-ratings initial-ratings game-inv))))))

(deftest compute-rankings-test
  (testing "Passing in all the games computes in the right order returns the rankings"
    (is (= {:c 1484.0991599102267, :b 1454.1965245402773, :a 1465.704315549496}
           (sut/compute-rankings games)))))

(deftest normalize-game-test
  (testing "Shoul compute correctly"
    (are [out game] (= out (sut/normalize-game game))
      ["Me" "You" 1] {:p1-name "Me" :p2-name "You" :p1-goals 2 :p2-goals 1}
      ["You" "Me" 0.5] {:p1-name "You" :p2-name "Me" :p1-goals 1 :p2-goals 1})))
