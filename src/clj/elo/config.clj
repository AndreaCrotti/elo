(ns elo.config
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [clojure.java.io :as io]
            [aero.core :as aero]))

(defn- load-config
  []
  (let [profile (:environment env)]
    (log/info "loading configuration for profile " profile)
    (aero/read-config (io/resource "config/base.edn")
                      {:profile profile})))

(defn value
  [k]
  (let [config (load-config)]
    (assert (contains? config k) "Missing configuration")
    (-> config k)))
