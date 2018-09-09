(ns elo.core
  (:require [accountant.core :as accountant]
            [cemerick.url :refer [url]]
            [elo.league-detail.views :as league-detail-views]
            [elo.league-detail.handlers :as league-detail-handlers]
            [elo.league-list.views :as league-list-views]
            [elo.league-list.handlers :as league-list-handlers]
            [elo.routes :as routes]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            #_[taoensso.timbre :as timbre :refer-macros [log info debug]]))

(def pages
  {:league-detail league-detail-views/root
   :league-list league-list-views/root})

(defn- path-exists? [path]
  (boolean (routes/match-route path)))

(def debug?
  ^boolean js/goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)))

(defn mount-root [page]
  (js/console.log "Calling mount root with page = " page)
  (re-frame/clear-subscription-cache!)
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defn nav-handler
  [path]
  (js/console.log "Calling nav handler with path" path)
  (let [new-handler (routes/match-route path)
        new-page (get pages (:handler new-handler))]

    (re-frame/dispatch [:set-route-params (:route-params new-handler)])
    (js/console.log "Switching to page" new-handler)
    (mount-root new-page)))

(defn curr-path
  []
  (->
   js/window.location.href
   url
   :path))

(defn ^:export init []
  (re-frame/dispatch-sync [::league-list-handlers/initialize-db])
  (re-frame/dispatch-sync [::league-detail-handlers/initialize-db])

  (dev-setup)
  (js/console.log "Configuring the routing navigation")
  (accountant/configure-navigation!
   {:nav-handler nav-handler
    :path-exists? path-exists?})

  ;;TODO: is it necessary to do this at all?
  (nav-handler (curr-path)))
