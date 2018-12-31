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
 (fn [{:keys [db]} [_ {:keys [page]}]]
   ;;TODO: add support for filters as well
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
  ;; pushy is here to take care of nice looking urls. Normally we would have to
  ;; deal with #. By using pushy we can have '/about' instead of '/#/about'.
  ;; pushy takes three arguments:
  ;; dispatch-fn - which dispatches when a match is found
  ;; match-fn - which checks if a route exist
  ;; identity-fn (optional) - extract the route from value returned by match-fn
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def history (pushy/pushy dispatch-route (match-route routes)))

(defn set-token!
  [token]
  (pushy/set-token! history token))
