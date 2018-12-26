(ns elo.user.handlers
  (:require [elo.common.handlers :as common]
            [re-frame.core :as rf]))

(def db
  {:opponent nil})

(def page ::page-id)

;;TODO: pass the spec
(def setter (partial common/setter* page))
(def getter (partial common/getter* page))

(rf/reg-sub ::opponent (getter [:opponent]))
(rf/reg-event-db ::opponent (setter [:opponent]))
