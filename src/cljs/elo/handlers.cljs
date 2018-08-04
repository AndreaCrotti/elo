(ns elo.handlers
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(def default-db
  {:games []
   :rankings []
   :game {}})

(defn- getter
  [key]
  (fn [db _]
    (key db)))

(rf/reg-sub :rankings (getter :rankings))
(rf/reg-sub :games (getter :games))

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

(rf/reg-event-fx :submit-success
                 (fn [{:keys [db]} [_ value]]
                   {:db db
                    :dispatch [:load-games]}))

(rf/reg-event-db :failed
                 (fn [db [_ response]]
                   (js/console.log "Failed request " response)
                   db))

(rf/reg-event-db :load-games-success
                 (fn [db [_ games]]
                   (assoc db :games games)))

(rf/reg-event-db :load-rankings-success
                 (fn [db [_ rankings]]
                   (assoc db :rankings rankings)))

(defn load-games
  [{:keys [db]} _]
  {:db db
   :http-xhrio {:method :get
                :uri "/games"
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:load-games-success]
                :on-failure [:failed]}})

(rf/reg-event-fx :load-games load-games)

(defn load-rankings
  [{:keys [db]} _]
  {:db db
   :http-xhrio {:method :get
                :uri "/rankings"
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:load-rankings-success]
                :on-failure [:failed]}})

(rf/reg-event-fx :load-rankings load-rankings)

(defn submit
  [{:keys [db]} [_ value]]
  {:db db
   :http-xhrio {:method :post
                :uri "/store"
                :params (:game db)
                :format (ajax/json-request-format)
                :response-format (ajax/json-response-format {:keywords? true})
                :on-success [:submit-success]
                :on-failure [:failed]}})

(rf/reg-event-fx :submit submit)
