(ns byf.datascript
  "Possible use of datascript"
  (:require [datascript.core :as d]
            [clj-time.core :as t])
  (:import (java.util UUID)))

(def schema {:player/name {:db/index true}
             :game/t1 {:db/index true}
             :game/t2 {:db/index true}
             :game/p1 {:db/valueType :db.type/ref}
             :game/p2 {:db/valueType :db.type/ref}
             :game/p1-points {}
             :game/p2-points {}
             :game/time {}})

(def conn (d/create-conn schema))

;; find all the games from a given player

(def datoms
  [{:player/name "Andrea" :db/id -1}
   {:player/name "Giorgio" :db/id -2}
   {:game/t1 "Juventus"
    :game/t2 "PSG"
    :game/p1 -2
    :game/p2 -1
    :game/p1-points 2
    :game/p2-points 3
    :game/time (t/now)}

   {:game/t1 "Real Madrid"
    :game/t2 "Barcelona"
    :game/p1 -1
    :game/p2 -2
    :game/p1-points 2
    :game/p2-points 3
    :game/time (t/now)}])

(d/transact! conn datoms)

;; instead of :game/t1 we could do :game/t1.team for example

;; find all the teams that a given player used
(def all-teams-from-player
  '[:find ?team-name
    :in $ ?name
    :where
    ;; find the player with name ?name
    [?e :player/name ?name]
    ;; find the p1 id with all the matches where p1 plays
    [?g :game/p1 ?e]
    ;; find what team was used
    [?g :game/t1 ?team-name]])

(d/q all-teams-from-player
     @conn "Andrea")
