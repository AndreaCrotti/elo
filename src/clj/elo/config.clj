(ns elo.config
  (:require [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [aero.core :as aero]))

(def config (atom {}))

(defn load-config
  []
  (log/info "loading configuration for profile "
            (:environment env))

  (let [cfg (aero/read-config "config.edn" {:profile (:environment env)})]
    (reset! config cfg)))

(load-config)
