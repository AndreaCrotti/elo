(ns byf.common.players
  (:require [byf.common.handlers :as common]
            [clojure.spec.alpha :as s]
            [byf.games :as games]
            [re-frame.core :as rf]))

;;TODO: add more to the db?
(def db
  {:players []})

(s/def ::players (s/coll-of (s/keys :req-un [::name
                                             ::user_id
                                             ::league_id
                                             ::player_id])))

(rf/reg-sub ::active-players
            :<- [::players]

            (fn [players]
              (->> players
                   (filter :active)
                   (map :id)
                   set)))

(rf/reg-sub ::name-mapping
            :<- [::players]

            (fn [players _]
              (games/player->names players)))

(rf/reg-sub ::active-players-names
            :<- [::active-players]
            :<- [::name-mapping]

            (fn [[active-players name-mapping]]
              (set (map name-mapping active-players))))

(s/def ::db (s/keys :req-un [::players]))

(def page ::page-id)

;;TODO: pass the spec
(def setter (partial common/setter* page))
(def getter (partial common/getter* page))

(rf/reg-sub ::players (getter [:players]))

(rf/reg-event-db ::load-players-success (setter [:players]))

(rf/reg-event-fx ::load-players (common/loader "/api/players" ::load-players-success))
