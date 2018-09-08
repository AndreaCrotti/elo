(ns elo.core
  (:require [accountant.core :as accountant]
            [bidi.bidi :as bidi]
            [elo.league-detail.handlers :as handlers]
            [elo.league-detail.views :as league-detail-views]
            [elo.routes :refer [routes]]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            #_[taoensso.timbre :as timbre :refer-macros [log info debug]]))

(def pages
  {:league-detail league-detail-views
   :league-list (constantly true)})

(def path-for (partial bidi/path-for routes))

(def match-route (partial bidi/match-route routes))

(defn- path-exists? [path]
  (boolean (bidi/match-route routes path)))

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
  (let [new-handler (bidi/path-for path)
        new-page (pages new-handler)]

    (js/console.log "Switching to page" new-handler)
    (mount-root new-page)))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (accountant/configure-navigation!
   {:nav-handler nav-handler
    :path-exists? path-exists?})

  #_(mount-root league-detail-views/root))
