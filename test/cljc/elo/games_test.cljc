(ns elo.games-test
  (:require [clojure.test :refer [deftest testing are is]]
            [elo.generators :as gen]
            [elo.games :as sut]))

(deftest normalize-game-test
  (testing "Shoul compute correctly"
    (are [out game] (= out (sut/normalize-game game))
      [:p1 :p2 1 0] {:game :fifa :p1 :p1 :p2 :p2
                     :p1_goals 3 :p2_goals 0}
      [:p1 :p2 0.5 0.5] {:game :fifa :p1 :p1 :p2 :p2
                         :p1_goals 2 :p2_goals 2})))

(deftest get-rankings-test
  (testing "Compute Fifa rankings"
    
    (let [[p1 p2] [(gen/player-gen {:name "p1"}) (gen/player-gen {:name "p2"})]
          g1 (gen/game-gen {:p1 (:id p1)
                            :p2 (:id p2)
                            :p1_goals 2
                            :p2_goals 0})

          rankings (sut/get-rankings [g1] [p1 p2])]

      (is (= [{:id (:id p1)
               :ranking 1516.0
               :ngames 1}

              {:id (:id p2)
               :ranking 1484.0
               :ngames 1}]

             rankings)))))
