(ns ^:figwheel-hooks byf.core
  (:require [accountant.core :as accountant]
            [byf.admin.handlers :as admin-handlers]
            [byf.config :as config]
            [byf.admin.views :as admin-views]
            [byf.firebase :as firebase]
            [byf.league-detail.handlers :as league-detail-handlers]
            [byf.league-detail.views :as league-detail-views]
            [byf.league-list.handlers :as league-list-handlers]
            [byf.league-list.views :as league-list-views]
            [byf.routes :as routes]
            [byf.user.views :as user-views]
            [cemerick.url :refer [url]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(def pages
  {:league-detail league-detail-views/root
   :league-list   league-list-views/root
   :admin         admin-views/root
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
  (-> js/window.location.href
      url
      :path))

(defn get-current-page
  []
  (->> (curr-path)
       routes/match-route
       :handler
       (get pages)))

(defn mount-root
  [page]
  (rf/clear-subscription-cache!)
  (reagent/render [page]
                  (.getElementById js/document "app")))

(defn ^:after-load reload-hook
  []
  (mount-root (get-current-page)))

(defn nav-handler
  [path]
  (let [new-handler (routes/match-route path)]
    (rf/dispatch [:set-route-params (:route-params new-handler)])
    (reload-hook)))

(defn ^:export init []
  (rf/dispatch-sync [::league-list-handlers/initialize-db])
  (rf/dispatch-sync [::league-detail-handlers/initialize-db])
  (rf/dispatch-sync [::admin-handlers/initialize-db])
  (when (config/value :auth-enabled)
    (firebase/init))

  (dev-setup)
  (accountant/configure-navigation!
   {:nav-handler  nav-handler
    :path-exists? path-exists?})
  (nav-handler (curr-path)))

