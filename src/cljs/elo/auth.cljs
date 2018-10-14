(ns elo.auth
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [elo.common.handlers :as common]
            [re-frame.core :as rf]))

(def page ::page-id)

(rf/reg-event-db ::set-authentication
                 (fn [db [_ value]]
                   (common/assoc-in* db page [:authenticated] value)))

(defn authenticated?
  [{:keys [db]}]

  {:db db
   :http-xhrio {:method :get
                :uri "/api/is-authenticated"
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [::set-authentication]
                :on-failure [:failed]}})

(rf/reg-event-fx ::authenticated? authenticated?)
