(ns byf.config
  (:require [aero.core :as aero]
            [clojure.java.io :as io]
            [environ.core :refer [env]]))

(defonce config (atom nil))

(defn load-config
  []
  (let [profile (:environment env)]
    ;; the dev profile always reloads even `config.edn`
    (when (or (= :dev profile)
              (nil? @config))
      (reset! config (aero/read-config (io/resource "config.edn")
                                       {:profile profile})))

    (if-let [user (io/resource "user.edn")]
      (merge @config (aero/read-config user))
      @config)))

(defn value
  [k]
  (let [config (load-config)]
    (assert (contains? config k))
    (get config k)))
