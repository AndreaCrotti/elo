(ns byf.common.games
  (:require [byf.common.handlers :as common]
            [byf.common.sets :as sets]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))
