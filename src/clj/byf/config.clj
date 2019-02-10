(ns byf.config
  (:require [environ.core :refer [env]]
            [aero.core :as aero]))

(defonce config (atom nil))

(defn load-config
  []
  (let [profile (:environment env)]
    ;; the dev profile always reloads even `config.edn`
    (if (or (= :dev profile)
            (nil? @config))
      (reset! config (aero/read-config "config.edn" {:profile profile})))
    ;; if there is a `user.edn` file load that as well and merge it
    (if (.exists (java.io.File. "user.edn"))
      (merge @config (aero/read-config "user.edn"))
      @config)))

(defn value
  [k]
  (let [config (load-config)]
    (assert (contains? config k))
    (get config k)))
