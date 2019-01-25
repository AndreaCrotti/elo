(ns elo.config
  (:require [environ.core :refer [env]]))

(def google-analytics-tag (:google-analytics-tag env))

(def github-client-id (:github-client-id env))
(def github-client-secret (:github-client-secret env))

(def auth-enabled (:auth-enabled env))
