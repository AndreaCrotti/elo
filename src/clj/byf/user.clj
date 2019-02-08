(ns byf.user
  (:require [expound.alpha :as expound]
            [clojure.spec.alpha :as s]))

(s/check-asserts true)
(alter-var-root #'s/*explain-out* (constantly expound/printer))
