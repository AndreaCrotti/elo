(ns elo.common.games
  (:require [elo.common.handlers :as common]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))
