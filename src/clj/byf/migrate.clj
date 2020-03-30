(ns byf.migrate
  (:gen-class)
  (:require [migratus.core :as migratus]
            [byf.config :refer [value]]))

(defn config
  []
  {:store         :database
   :migration-dir "migrations"
   :db            (value :database-url)})

(defn -main
  [& args]
  (migratus/migrate (config)))
