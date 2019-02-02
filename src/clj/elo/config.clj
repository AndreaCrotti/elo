(ns elo.config
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [aero.core :as aero]))

(defn- load-config
  []
  (let [profile (:environment env)]
    (log/info "loading configuration for profile " profile)
    (aero/read-config "config.edn" {:profile profile})))

(defn value
  [k]
  (let [config (load-config)]
    (assert (contains? config k) "Missing configuration")
    (-> config k)))
