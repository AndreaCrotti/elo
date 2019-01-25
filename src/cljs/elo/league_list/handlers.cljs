(ns elo.league-list.handlers
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [elo.common.handlers :as common]
            [re-frame.core :as rf]))

;;TODO: use the path interceptor instead of this

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
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(rf/reg-event-fx ::load-leagues
                 (loader page "/api/leagues" ::load-leagues-success))

(rf/reg-sub ::leagues (getter [:leagues]))

(defn auth-success
  [db [_ provider]]
  (common/assoc-in* db page
                    [:authenticatetion]
                    provider))

(defn oauth2-auth
  [{:keys [db]} [_ provider]]
  {:http-xhrio {:method :post
                :uri (str "/oauth2/" provider)
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:auth-success]
                :on-failure [:failed]}})

(rf/reg-event-fx :oauth2-auth oauth2-auth)

(rf/reg-event-db :auth-success auth-success)
