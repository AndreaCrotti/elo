(ns ^:figwheel-hooks elo.core
  (:require [cemerick.url :refer [url]]
            [elo.admin.handlers :as admin-handlers]
            [elo.auth :as auth]
            [elo.history :as history]
            [elo.league-detail.handlers :as league-detail-handlers]
            [elo.league-list.handlers :as league-list-handlers]
            [elo.root.views :as root-views]
            [elo.routes :as routes]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(def debug?
  ^boolean js/goog.DEBUG)

(defn dev-setup []
  (when debug?
    (enable-console-print!)))

(defn curr-path
  []
  (->
   js/window.location.href
   url
   :path))

(defn mount-root
  [page]
  (re-frame/clear-subscription-cache!)
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defn ^:after-load reload-hook
  []
  (mount-root root-views/root))

(defn nav-handler
  [path]
  (let [new-handler (routes/match-route path)]
    (re-frame/dispatch [:set-route-params (:route-params new-handler)])
    (reload-hook)))

(defn ^:export init []
  ;; this should still be done only once
  ;; do we need to initialise everything??
  ;; can we dispatch multiple in one go?
  ;; TODO: we can probably just load at most once
  (re-frame/dispatch-sync [::league-list-handlers/initialize-db])
  (re-frame/dispatch-sync [::league-detail-handlers/initialize-db])
  (re-frame/dispatch-sync [::admin-handlers/initialize-db])
  (re-frame/dispatch-sync [::auth/authenticated?])

  (dev-setup)
  (history/start!)
  
  (reload-hook))
