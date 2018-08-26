(ns elo.games-test
  (:require [clojure.test :refer [deftest testing are]]
            [elo.games :as sut]))

(deftest normalize-game-test
  (testing "Shoul compute correctly"
    (are [out game] (= out (sut/normalize-game game))
      [:p1 :p2 1 0] {:game :fifa :p1 :p1 :p2 :p2
                     :p1_goals 3 :p2_goals 0}
      [:p1 :p2 0.5 0.5] {:game :fifa :p1 :p1 :p2 :p2
                         :p1_goals 2 :p2_goals 2})))
