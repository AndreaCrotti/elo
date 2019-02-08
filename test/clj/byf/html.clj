(ns byf.html
  (:require [clojure.test :refer [deftest testing is]]
            [byf.pages.home :as home]))

(deftest homepage-gen-test
  (testing "Homepage generated correctly"
    (is (some? (home/body {})))))
