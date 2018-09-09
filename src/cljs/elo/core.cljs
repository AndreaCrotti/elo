(ns elo.core
  (:require [accountant.core :as accountant]
            [cemerick.url :refer [url]]
            [elo.league-detail.handlers :as league-detail-handlers]
            [elo.league-detail.views :as league-detail-views]
            [elo.league-list.handlers :as league-list-handlers]
            [elo.league-list.views :as league-list-views]
            [elo.admin.views :as admin-views]
            [elo.routes :as routes]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

;;TODO: use this to set dynamically the title of the page you are in
(defn- set-title!
  [new-title]
  (set! js/document.title new-title))

(def pages
  {:league-detail league-detail-views/root
   :league-list league-list-views/root
   :admin admin-views/root})

(defn- path-exists? [path]
  (boolean (routes/match-route path)))

(def debug?
  ^boolean js/goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)))

(defn mount-root [page]
  (re-frame/clear-subscription-cache!)
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defn nav-handler
  [path]
  (let [new-handler (routes/match-route path)
        new-page (get pages (:handler new-handler))]

    (re-frame/dispatch [:set-route-params (:route-params new-handler)])
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

  (js/console.log "In core/init")
  (dev-setup)
  (accountant/configure-navigation!
   {:nav-handler nav-handler
    :path-exists? path-exists?})

  (nav-handler (curr-path)))
