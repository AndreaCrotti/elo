(ns ^:figwheel-hooks byf.core
  (:require [accountant.core :as accountant]
            [cemerick.url :refer [url]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [byf.league-detail.handlers :as league-detail-handlers]
            [byf.league-detail.views :as league-detail-views]
            [byf.league-list.handlers :as league-list-handlers]
            [byf.league-list.views :as league-list-views]
            [byf.admin.views :as admin-views]
            [byf.admin.handlers :as admin-handlers]
            [byf.user.views :as user-views]
            [byf.auth :as auth]
            [byf.routes :as routes]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]))

(def pages
  {:league-detail league-detail-views/root
   :league-list league-list-views/root
   :admin admin-views/root
   :player-detail user-views/root})

(defn- path-exists? [path]
  (boolean (routes/match-route path)))

(def debug?
  ^boolean js/goog.DEBUG)

(defn dev-setup []
  (when debug?
    (set! s/*explain-out* expound/printer)
    (s/check-asserts true)
    (enable-console-print!)))

(defn curr-path
  []
  (->
   js/window.location.href
   url
   :path))

(defn get-current-page
  []
  (let [path (curr-path)
        route (routes/match-route path)]
    (get pages (:handler route))))

(defn mount-root
  [page]
  (re-frame/clear-subscription-cache!)
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defn ^:after-load reload-hook
  []
  (mount-root (get-current-page)))

(defn nav-handler
  [path]
  (let [new-handler (routes/match-route path)]
    (re-frame/dispatch [:set-route-params (:route-params new-handler)])
    (reload-hook)))

(defn ^:export init []
  (re-frame/dispatch-sync [::league-list-handlers/initialize-db])
  (re-frame/dispatch-sync [::league-detail-handlers/initialize-db])
  (re-frame/dispatch-sync [::admin-handlers/initialize-db])
  (re-frame/dispatch-sync [::auth/authenticated?])

  (dev-setup)
  (accountant/configure-navigation!
   {:nav-handler nav-handler
    :path-exists? path-exists?})

  (nav-handler (curr-path)))
