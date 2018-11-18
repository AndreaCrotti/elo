(ns elo.pages.home
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]
            [elo.routes :as routes]
            [clojure.data.json :as json]))

(defn client-side-config
  [request]
  (let [league-id
        (-> request
            :uri
            routes/match-route
            :route-params
            :league-id)]

    (-> {:league_id league-id}
        json/write-str)))

(defn body
  [request]
  [:html
   (gen-header "League page")
   [:body
    [:div {:id "app"}]
    [:script (format "window['config']=%s" (client-side-config request))]
    [:script {:src (cache-buster "/cljs-out/elo-main.js")}]
    [:script {:src (cache-buster "/js/playground.js")}]

    [:script "elo.core.init();"]]])
