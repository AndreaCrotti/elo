(ns user
  (:require [integrant.repl :as ig]
            [expound.alpha :as e]
            [clojure.spec.alpha :as s]
            [elo.dev :as dev]))

(def printer
  (e/custom-printer {:show-valid-values? true
                     :print-specs? true
                     :theme :figwheel-theme}))

(alter-var-root #'s/*explain-out* (constantly printer))

(s/check-asserts true)

(ig/set-prep! (constantly (select-keys dev/config
                                       [:server/jetty])))
