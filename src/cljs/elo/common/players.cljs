(ns elo.common.players
  (:require [elo.common.handlers :as common]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))

(rf/reg-sub ::players (getter [:players]))

(rf/reg-event-db ::load-players-success (setter [:players]))

(rf/reg-event-fx ::load-players (common/loader page "/api/players" ::load-players-success))
