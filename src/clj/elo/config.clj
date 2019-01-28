(ns elo.config
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [aero.core :as aero]))

(def config (atom {}))

(def google-analytics-tag (:google-analytics-tag env))

(def github-client-id (:github-client-id env))
(def github-client-secret (:github-client-secret env))

(def auth-enabled (:auth-enabled env))

(defn load-config
  []
  (log/info "loading configuration for profile "
            (:environment env))

  (let [cfg (aero/read-config "config.edn" {:profile (:environment env)})]
    (reset! config cfg)))

(load-config)
