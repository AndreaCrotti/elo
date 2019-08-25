(ns byf.auth
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [byf.common.handlers :as common]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))
(def getter (partial common/getter* page))

(def default-db
  {:current-user nil})

(rf/reg-sub ::current-user (getter [:current-user]))

(rf/reg-event-db ::set-current-user (setter [:current-user]))

(rf/reg-event-db ::set-authentication
                 (fn [db [_ auth-details]]
                   (common/assoc-in* db page [:auth] auth-details)))

(rf/reg-sub ::authenticated?
            (fn [db _]
              (common/get-in* db page [:auth :authenticated])))

(defn authenticated?
  [_]
  {:http-xhrio {:method :get
                :uri "/authenticated"
                :format common/edn-request-format
                :response-format common/edn-response-format
                :on-success [::set-authentication]
                :on-failure [:failed]}})

(rf/reg-event-fx ::authenticated? authenticated?)
