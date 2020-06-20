(ns byf.league-list.handlers
  (:require [day8.re-frame.http-fx]
            [byf.common.handlers :as common]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))

(def default-db
  {:leagues []})

(rf/reg-event-db ::initialize-db
                 (fn [db _]
                   (assoc db page default-db)))

(rf/reg-event-db ::load-leagues-success (setter [:leagues]))

(defn loader
  [page uri on-success]
  (fn [{:keys [db]} _]
    {:http-xhrio {:method :get
                  :uri uri
                  :format common/request-format
                  :response-format common/response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(rf/reg-event-fx ::load-leagues
                 (loader page "/api/leagues" ::load-leagues-success))

(rf/reg-sub ::leagues (getter [:leagues]))
