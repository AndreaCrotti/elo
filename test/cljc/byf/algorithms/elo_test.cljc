(ns byf.algorithms.elo-test
  (:require [clojure.test :refer [deftest is are testing]]
            [byf.generators :as gen]
            [byf.shared-config :as shared]
            [byf.algorithms.elo :as sut]))

;; define a more generic game representation, that doesn't require
;; some form of normalization
(def games
  [[:a :b 1 0]
   [:b :c 0.5 0.5]
   [:a :c 0 1]])

(def initial-rankings
  (sut/initial-rankings [:a :b :c]))

(deftest new-rating-test
  (testing "Should compute the new rating correctly"
    (are [d exp] (= exp (sut/expected d))
      10 0.48561281583400134
      (- 10) 0.5143871841659987)))

(deftest byf-rating-test
  (testing "Should be a zero sum game"
    (let [game [:a :b 0 1]
          rs (sut/new-rankings {:a 1500 :b 1500} game)]
      (is (== 3000
              (apply + (vals rs))))))

  (testing "Order does not matter"
    (let [game [:a :b 1 0]
          game-inv [:b :a 0 1]]

      (is (= {:a 1516.0, :b 1484.0, :c 1500}
             (sut/new-rankings initial-rankings game)
             (sut/new-rankings initial-rankings game-inv))))))

(deftest compute-rankings-test
  (testing "Passing in all the games computes in the right order returns the rankings"
    (is (= {:c 1516.0338330211207, :b 1484.736306793522, :a 1499.2298601853572}
           (sut/compute-rankings games [:a :b :c]))))

  (testing "passing new players sets them up with an initial ranking"
    (is (= {:a 1499.2298601853572,
            :b 1484.736306793522,
            :c 1516.0338330211207,
            :d 1500}
           (sut/compute-rankings games [:a :b :c :d])))))

(defn- avg [xs] (/ (apply + xs) (count xs)))

;;TODO: can probably use checking instead
(deftest average-not-changing
  (let [gs (repeatedly 100 gen/game-gen)
        sg-norm (map sut/normalize-game gs)]

    (testing "Rankings average is stable"
      (is (== (:initial-ranking shared/default-game-config)
              (-> sg-norm
                  (sut/compute-rankings
                   (sut/extract-players sg-norm)
                   shared/default-game-config)
                  vals
                  avg))))

    (testing "Rankings average is stable without default values"
      (is (== 100.00
              (-> sg-norm
                  (sut/compute-rankings
                   (sut/extract-players sg-norm)
                   {:initial-ranking 100
                    :k 5})
                  vals
                  avg))))))
