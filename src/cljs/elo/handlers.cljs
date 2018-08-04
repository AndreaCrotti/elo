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

(rf/reg-event-db :p1-goals (setter [:game :p1-goals]))
(rf/reg-event-db :p1-name (setter [:game :p1-name]))
(rf/reg-event-db :p1-team (setter [:game :p1-team]))

(rf/reg-event-db :p2-goals (setter [:game :p2-goals]))
(rf/reg-event-db :p2-name (setter [:game :p2-name]))
(rf/reg-event-db :p2-team (setter [:game :p2-team]))

(defn submita
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
