(ns byf.datascript
  (:require [datascript.core :as d]
            [clj-time.core :as t])
  (:import (java.util UUID)))

(defn gen-uuid [] (UUID/randomUUID))

(def schema {:player/name {:db/index true}
             :game/team {:db/index true}
             :game/p1 {:db/valueType :db.type/ref}
             :game/p2 {:db/valueType :db.type/ref}
             ;; :game/p1-goals {}
             ;; :game/p2-goals {}
             ;; :game/time {:db/valueType :db.type/instant}
             })

(def conn (d/create-conn schema))

(declare render persist)

(defn reset-conn [db]
  (reset! conn db)
  (render db)
  (persist db))

;; find all the games from a given player

(def datoms
  [{:player/name "Andrea" :db/id 1}
   {:player/name "Giorgio" :db/id 2}
   {:game/team "Real Madrid" :game/p1 1 :game/p2 2}
   ])

(d/transact! conn datoms)

(def q '[:find ?p
         :in $ ?name
         :where
         [?e :player/name ?name]
         [?p :game/p1 ?e]])

(d/q q @conn "Andrea")

