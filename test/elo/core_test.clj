(ns elo.core-test
  (:require [clojure.test :refer [deftest is are testing]]
            [elo.core :as elo]))

(def games
  [[:a :b 1]
   [:b :c 0.5]
   [:a :c (- 1)]])

(def initial-ratings
  {:a 1500
   :b 1500
   :c 1500})

(deftest elo-rating
  (testing "Should compute new ratings correctly"
    (is (= {:a 1465.704315549496, :b 1454.1965245402773, :c 1484.0991599102267}
           (elo/update-ratings initial-ratings games))))

  (testing "Order does not matter"
    (let [game [:a :b 1]
          game-inv [:b :a (- 1)]]

      (is (= {:a 1516.0, :b 1452.0, :c 1500}
             (elo/new-ratings initial-ratings game)
             (elo/new-ratings initial-ratings game-inv))))))
