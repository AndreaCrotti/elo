(ns elo.db
  (:require [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]))

(def db-spec (env :database-url))

(defn store
  [params]
  (-> (h/insert-into :games)
      (h/values params)))

(defn load-games
  []
  (-> (h/select :*)
      (h/from :games)))
