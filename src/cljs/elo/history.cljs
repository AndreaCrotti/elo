(ns elo.history
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [elo.routes :refer [routes]]
            [re-frame.core :as rf]))

(defn- match-route
  [url]
  (partial bidi/match-route routes))

(defn parse-url
  [url]
  (match-route url))

(rf/reg-event-fx
 ::set-active-page
 [rf/debug]
 (fn [{:keys [db]} [_ {:keys [page] :as obj}]]
   (js/console.log "Page = " page
                   "obj = " obj)
   (let [set-page (assoc db :active-page page)
         events
         (case page
           :league-list [[:elo.league-list.handlers/load-leagues]]
           :league-detail [[:elo.league-detail.handlers/load-games]
                           [:elo.league-detail.handlers/load-league]
                           [:elo.common.players/load-players]]

           :admin [[:elo.admin.handlers/load-leagues]])]

     {:db set-page
      :dispatch-n events})))

(defn dispatch-route
  [matched-route]
  (rf/dispatch [::set-active-page {:page (:handler matched-route)}]))

(defn start!
  []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def history (pushy/pushy dispatch-route (match-route routes)))

(defn set-token!
  [token]
  (pushy/set-token! history token))
