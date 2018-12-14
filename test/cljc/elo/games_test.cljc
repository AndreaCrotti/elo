(ns elo.games-test
  (:require [clojure.test :refer [deftest testing are is]]
            [elo.generators :as gen]
            [elo.games :as sut]))

(deftest normalize-game-test
  (testing "Should normalize games correctly"
    (are [out game] (= out (sut/normalize-game game))
      [:p1 :p2 1 0] {:game :fifa :p1 :p1 :p2 :p2
                     :p1_points 3 :p2_points 0}

      [:p1 :p2 0.5 0.5] {:game :fifa :p1 :p1 :p2 :p2
                         :p1_points 2 :p2_points 2})))

(deftest get-rankings-test
  (testing "Should compute the rankings correctly"
    (let [[p1 p2] [(gen/player-gen {:name "p1"})
                   (gen/player-gen {:name "p2"})]

          g1 (gen/game-gen {:p1 (:id p1)
                            :p2 (:id p2)
                            :p1_points 2
                            :p2_points 0})

          rankings (sut/get-rankings [g1] [p1 p2])]

      (is (= [{:id (:id p1)
               :ranking 1516.0
               :ngames 1}

              {:id (:id p2)
               :ranking 1484.0
               :ngames 1}]

             rankings)))))

(deftest results-summary-test
  (testing "Should be able to summarize raw results"
    (are [summary games] (= summary (sut/summarise games))
      ;; one single game should be extracted correctly
      {:p1 {:wins 1 :losses 0 :draws 0 :points-done 3 :points-received 0}
       :p2 {:wins 0 :losses 1 :draws 0 :points-done 0 :points-received 3}}

      [{:game :fifa :p1 :p1 :p2 :p2
        :p1_points 3 :p2_points 0}])))

(deftest last-games-result-test
  (testing "Should be able to compute the results for all the games"
    (is (= {:p1 [:w] :p2 [:l]}
           (sut/results
            [{:game :fifa :p1 :p1 :p2 :p2
              :p1_points 3 :p2_points 0}])))))

(deftest rankings-history-test
  (testing "Rankings history returned correctly"
    (let [games [{:p1 1 :p2 2 :p1_points 0 :p2_points 0 :played_at "2018-10-18T14:15:03.889Z"}]
          players [{:id 1 :name "P1"} {:id 2 :name "P2"} {:id 3 :name "P3"}]
          desired [{:ranking 1500,
                    :player "P2",
                    :game-idx 0,
                    :time "2018-10-18T14:15:03.889Z"
                    :result "P1 vs P2: (0 - 0)"}

                   {:ranking 1500,
                    :player "P1",
                    :game-idx 0,
                    :time "2018-10-18T14:15:03.889Z"
                    :result "P1 vs P2: (0 - 0)"}]]

      (is (= desired (sut/rankings-history players games))))))

(deftest longest-streaks-test
  (testing "Compute best streaks"
    (are [series streak] (= streak (sut/longest-winning-subseq series))
      [:w :w] 2
      [:l :d :w :w] 2
      [:l :w :w :d :w :w :w] 3)))

(deftest highest-points-test
  (testing "Compute highest point streaks"
    (are [series increase] (= increase (sut/highest-points-subseq series))
      [1 3 10] 9
      [10] 0
      [1 2 10 3 5 20] 17
      [1 2 3 4 3 4 8 8] 5)))
