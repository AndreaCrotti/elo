(ns elo.auth
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [elo.common.handlers :as common]
            [re-frame.core :as rf]))

(def page ::page-id)

(rf/reg-event-db :failed
                 (fn [db _]
                   (common/assoc-in* db page [:failed] true)))

(rf/reg-event-db ::set-authentication
                 (fn [db [_ auth-details]]
                   (common/assoc-in* db page [:auth] auth-details)))

(rf/reg-sub ::authenticated?
            (fn [db _]
              (common/get-in* db page [:auth :authenticated])))

(defn authenticated?
  [{:keys [db]}]

  {:db db
   :http-xhrio {:method :get
                :uri "/authenticated"
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::set-authentication]
                :on-failure [:failed]}})

(rf/reg-event-fx ::authenticated? authenticated?)
