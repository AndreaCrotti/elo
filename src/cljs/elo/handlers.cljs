(ns elo.handlers
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

;;TODO: this might get defined too late anyway
(defn default-game
  [db]
  {:p1 (-> db :players first :id)
   :p2 (-> db :players first :id)
   :p1_goals "0"
   :p2_goals "0"
   :p1_team ""
   :p2_team ""})

(def default-db
  {:games []
   :rankings []
   :players []
   :game {}
   :player {}})

(defn- getter
  [key]
  (fn [db _]
    (key db)))

(defn- setter
  [key]
  (fn [db [_ val]]
    (assoc-in db key val)))


(rf/reg-sub :rankings (getter :rankings))
(rf/reg-sub :games (getter :games))
(rf/reg-sub :players (getter :players))
(rf/reg-event-db :initialize-db
                 (fn [db _]
                   (assoc default-db
                          :game
                          (default-game db))))

(rf/reg-event-db :p1 (setter [:game :p1]))
(rf/reg-event-db :p1_goals (setter [:game :p1_goals]))
(rf/reg-event-db :p1_team (setter [:game :p1_team]))

(rf/reg-event-db :p2 (setter [:game :p2]))
(rf/reg-event-db :p2_goals (setter [:game :p2_goals]))
(rf/reg-event-db :p2_team (setter [:game :p2_team]))

(rf/reg-event-db :name (setter [:player :name]))
(rf/reg-event-db :email (setter [:player :email]))

(rf/reg-event-fx :add-game-success
                 (fn [{:keys [db]} [_ value]]
                   {:db db
                    :dispatch-n [[:load-games]
                                 [:load-rankings]]}))

(rf/reg-event-db :failed
                 (fn [db [_ response]]
                   (js/console.log "Failed request " response)
                   db))

(rf/reg-event-db :load-games-success (setter [:games]))
(rf/reg-event-db :load-rankings-success (setter [:rankings]))
(rf/reg-event-db :load-players-success (setter [:players]))

(defn- loader
  [uri on-success]
  (fn [{:keys [db]} _]
    {:db db
     :http-xhrio {:method :get
                  :uri uri
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(rf/reg-event-fx :load-games (loader "/games" :load-games-success))
(rf/reg-event-fx :load-rankings (loader "/rankings" :load-rankings-success))
(rf/reg-event-fx :load-players (loader "/players" :load-players-success))

(defn writer
  [uri on-success params-fn]
  (fn [{:keys [db]} [_ value]]
    {:db db
     :http-xhrio {:method :post
                  :uri uri
                  :params (params-fn db)
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(rf/reg-event-fx :add-game (writer "/store" :add-game-success :game))
(rf/reg-event-fx :add-player (writer "/add-player" :add-player-success :player))
