(ns elo.handlers
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]))

(def default-db
  {:games []
   :rankings []
   :game {}})

#_(defn- getter
  [key]
  (fn [db _]
    (key db)))

(defn- setter
  [key]
  (fn [db [_ val]]
    (assoc-in db key val)))

(rf/reg-event-db :initialize-db
                 (fn [_ _]
                   default-db))

(rf/reg-event-db :p1_goals (setter [:game :p1_goals]))
(rf/reg-event-db :p1_name (setter [:game :p1_name]))
(rf/reg-event-db :p1_team (setter [:game :p1_team]))

(rf/reg-event-db :p2_goals (setter [:game :p2_goals]))
(rf/reg-event-db :p2_name (setter [:game :p2_name]))
(rf/reg-event-db :p2_team (setter [:game :p2_team]))

(defn submit
  [{:keys [db]} [_ value]]

  {:db db
   :http-xhrio {:method :post
                :uri "/store"
                :params (assoc (:game db) :coming value)
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:submit-success]
                :on-failure [:submit-failed]}})

(rf/reg-event-fx :submit submit)
