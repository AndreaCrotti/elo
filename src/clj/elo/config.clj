(ns elo.config
  (:require [environ.core :refer [env]]))

(def adsense-tag (:adsense-tag env))
(def google-analytics-tag (:google-analytics-tag env))

(def github-client-id (:github-client-id env))
(def github-client-secret (:github-client-secret env))
