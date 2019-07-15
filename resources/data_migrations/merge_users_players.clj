(ns data-migrations.merge-users-players
  (:require [byf.db :as db]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :as h]
            [honeysql.core :as sql]))

(defn merge-user-in-player
  []
  (doseq [p (db/query (fn [] (-> (h/select :*)
                                (h/from :player))))]))

(defn -main [& args]
  (merge-user-in-player))
