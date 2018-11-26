(ns elo.pages.home
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]
            [elo.routes :as routes]
            [environ.core :refer [env]]
            [clojure.data.json :as json]))

(defn- build-filename
  []
  (if (= (:dev env) "true")
    "/cljs-out/dev-main.js"
    "/cljs-out/prod-main.js"))

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
    [:script {:src (cache-buster (build-filename))}]

    [:script "elo.core.init();"]]])
