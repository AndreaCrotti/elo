(ns elo.common.handlers-test
  (:require [elo.common.handlers :as sut]
            [cljs.test :as t :refer-macros [deftest testing is]]))


(deftest get-in-test
  (testing "get in"
    (is (= "value"
           (sut/get-in* {::page {:key "value"}}
                        ::page
                        [:key])
           )))) 

(comment
  (cljs.test/run-tests))
