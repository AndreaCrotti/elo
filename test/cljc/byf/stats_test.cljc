(ns byf.stats-test
  (:require [byf.stats :as sut]
            #?(:clj [clojure.test :refer [deftest testing is are]]
               :cljs [cljs.test :as t :refer-macros [deftest testing is are]])))


(def sample-result
  {"fb6f256d-514b-479f-a70a-63987946de15"
   [:l :d :w :l :w :w :w :d]

   "edbebe16-dd1c-414e-b9b2-4c9e38d1928e"
   [:l :w :w :d]})

(def sample-mapping
  {"fb6f256d-514b-479f-a70a-63987946de15" "Charlie"
   "edbebe16-dd1c-414e-b9b2-4c9e38d1928e" "Fitz"})

(def history [{:ranking 1500
               :player "Emily"
               :game-idx 0
               :time "2017-01-24T00:31:50Z"
               :result "John vs Emily: (0 - 8)"}


              {:ranking 1516
               :player "Emily"
               :game-idx 1
               :time "2017-02-24T00:31:50Z"
               :result "John vs Emily: (0 - 8)"}])

(deftest highest-increase-test
  (testing "highest increase computation"
    (is (= [{:player "Emily", :points 16}]
           (sut/highest-increase history)))))



(deftest highest-rankings-best
  (testing "highest rankings best"
    (is (= [(last history)]
           (sut/highest-rankings-best history)))))

(deftest longest-streak-test
  (testing "longest streak"
    (is (= [{:player "Charlie", :streak 3} {:player "Fitz", :streak 2}]
           (sut/longest-streak sample-result sample-mapping)))))

(deftest longest-unbeaten-streak-test
  (testing "unbeaten for games"
    (is (= [{:player "Charlie", :streak 4} {:player "Fitz", :streak 3}]
           (sut/longest-unbeaten sample-result sample-mapping)))))

(deftest best-percents-test
  (testing "best percents"
    (is (= [{:player "Fitz", :w 50N, :d 25N, :l 25N}
            {:player "Charlie", :w 50N, :d 25N, :l 25N}]
           (sut/best-percents sample-result sample-mapping)))))
