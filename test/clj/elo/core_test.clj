(ns elo.core-test
  (:require [clojure.test :refer [deftest is are testing]]
            [elo.elo :as sut]))

(def games
  [[:a :b 1]
   [:b :c 0.5]
   [:a :c (- 1)]])

(def initial-ratings
  (sut/initial-rankings [:a :b :c]))

(deftest new-rating-test
  (testing "Should compute the new rating correctly"
    (are [d exp] (= exp (sut/expected d))
      10 0.48561281583400134
      (- 10) 0.5143871841659987))
  )

(deftest elo-rating-test
  #_(testing "Should compute new ratings correctly"
      (are [inp initial out] (= (sut/update-ratings initial inp) out)
        games initial-ratings {:a 1465.704315549496, :b 1454.1965245402773, :c 1484.0991599102267}
        [["A" "B" 1]] {"A" 100 "B" 100} {"A" 116.0, "B" 52.0}))

  (testing "Should be a zero sum game"
    (let [game [:a :b 1]
          rs (sut/new-ratings {:a 1500 :b 1500} game)]
      (is (= 3000
             (apply + (vals rs))))))

  #_(testing "Order does not matter"
      (let [game [:a :b 1]
            game-inv [:b :a (- 1)]]

        (is (= {:a 1516.0, :b 1452.0, :c 1500}
               (sut/new-ratings initial-ratings game)
               (sut/new-ratings initial-ratings game-inv))))))

(deftest compute-rankings-test
  (testing "Passing in all the games computes in the right order returns the rankings"
    (is (= {:c 1484.0991599102267, :b 1454.1965245402773, :a 1465.704315549496}
           (sut/compute-rankings games))))

  (testing "passing new players sets them up with an initial ranking"
    (is (= {:c 1484.0991599102267, :b 1454.1965245402773, :a 1465.704315549496 :d 1500}
           (sut/compute-rankings games [:a :b :c :d])))))

(deftest normalize-game-test
  (testing "Shoul compute correctly"
    (are [out game] (= out (sut/normalize-game game))
      ["Me" "You" 1] {:p1 "Me" :p2 "You" :p1_goals 2 :p2_goals 1}
      ["You" "Me" 0.5] {:p1 "You" :p2 "Me" :p1_goals 1 :p2_goals 1})))
