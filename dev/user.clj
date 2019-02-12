(ns user
  (:require [clojure.spec.alpha :as s]
            [expound.alpha :as e]
            [integrant.repl :as ir]
            [byf.api :refer [app]]
            [byf.config :refer [value]]))

(def printer
  (e/custom-printer {:show-valid-values? true
                     :print-specs? true
                     :theme :figwheel-theme}))

(alter-var-root #'s/*explain-out* (constantly printer))

(s/check-asserts true)

(def base-config
  {:server/jetty {:port (value :port)
                  :handler app}})

(ir/set-prep! (constantly base-config))
