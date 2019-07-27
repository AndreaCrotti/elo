(ns data-migrations.merge-users-players
  (:require [byf.db :as db]
            [clojure.java.jdbc :as jdbc]
            [honeysql.helpers :as h]
            [honeysql.core :as sql]))

(defn up
  [j]
  (->
   (h/update :player)
   (h/sset (select-keys j [:email :created_at :active]))
   (h/where [:= :player.user_id (:user_id j)])))

(defn merge-user-in-player
  []
  (doseq [joined
          (db/query (fn [] (-> (h/select :p.user_id :u.id :u.email :u.created_at :u.active)
                              (h/from [:player :p])
                              (h/join [:users :u]
                                      [:= :u.id :p.user_id]))))]
    (let [upd (sql/format (up joined))]
      (println "Running " upd)
      (jdbc/execute! (db/db-spec)
                     upd))))


(defn -main [& args]
  (merge-user-in-player))
