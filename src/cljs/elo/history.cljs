(ns elo.history
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [elo.routes :as routes]
            [re-frame.core :as rf]))

(defn- parse-url
  [url]
  (bidi/match-route routes/routes url))

(rf/reg-event-fx
 :set-active-page
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

(rf/reg-sub
 :active-page
 (fn [{:keys [db]}]
   (:active-page db)))

(defn dispatch-route
  [matched-route]
  (js/console.log "matched route = " matched-route)
  (rf/dispatch [:set-active-page {:page (:handler matched-route)}]))

(defn start!
  []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def history (pushy/pushy dispatch-route parse-url))

(defn set-token!
  [token]
  (pushy/set-token! history token))
