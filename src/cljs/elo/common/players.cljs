(ns elo.common.players
  (:require [elo.common.handlers :as common]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))

;;TODO: add more to the db?
(def db
  {:players []})

(s/def ::players (s/coll-of (s/keys :req-un [::name
                                             ::user_id
                                             ::league_id
                                             ::player_id])))
(s/def ::db (s/keys :req-un [::players]))

(def page ::page-id)

;;TODO: pass the spec
(def setter (partial common/setter* page))
(def getter (partial common/getter* page))

(rf/reg-sub ::players (getter [:players]))

(rf/reg-event-db ::load-players-success (setter [:players]))

(rf/reg-event-fx ::load-players (common/loader page "/api/players" ::load-players-success))
