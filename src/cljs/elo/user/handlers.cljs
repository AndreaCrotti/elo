(ns elo.user.handlers
  (:require [elo.common.handlers :as common]
            [re-frame.core :as rf]))

(def db
  {:opponent nil})

(def page ::page-id)

(rf/reg-sub ::player-id
            (fn [db _]
              (get-in db [:route-params :player-id])))

;;TODO: pass the spec
(def setter (partial common/setter* page))
(def getter (partial common/getter* page))

(rf/reg-sub ::opponent (getter [:opponent]))
(rf/reg-event-db ::opponent (setter [:opponent]))

(rf/reg-event-db ::load-player-success (setter [:player]))

;; now we can load all the player information, which includes the league
(rf/reg-event-fx ::load-player (common/loader-no-league-id page "/api/player" ::load-player-success))
