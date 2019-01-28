(ns elo.config
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [aero.core :as aero]))

(def config (atom {}))

(defn load-config
  []
  (let [profile (:environment env)]
    ;; failling on uberjar otherwise
    #_(assert (some? profile) "Could not detect profile")
    (log/info "loading configuration for profile " profile)
    (let [cfg (aero/read-config "config.edn" {:profile profile})]
      (reset! config cfg))))

(load-config)
