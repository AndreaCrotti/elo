(ns byf.pages.home
  (:require [byf.pages.utils :refer [cache-buster]]
            [byf.pages.header :refer [gen-header]]
            [byf.routes :as routes]
            [ring.middleware.anti-forgery :as anti-forgery]
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
  (let [csrf-token "hello" #_(force anti-forgery/*anti-forgery-token*)]
    [:html
     (gen-header "League page")
     [:body
      [:dive#sente-csrf-token {:data-csrf-token csrf-token}]
      [:div {:id "app"}]
      [:script (format "window['config']=%s" (client-side-config request))]
      [:script {:src (cache-buster "/cljs-out/dev-main.js")}]

      [:script "byf.core.init();"]]]))
