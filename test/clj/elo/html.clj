(ns elo.html
  (:require [clojure.test :refer [deftest testing is]]
            [elo.pages.home :as home]))

(deftest homepage-gen-test
  (testing "Homepage generated correctly"
    (is (some? (home/body {})))))
