(ns byf.pages.home
  (:require [byf.pages.utils :refer [cache-buster]]
            [byf.pages.header :refer [gen-header]]
            [byf.routes :as routes]
            [clojure.data.json :as json]))

;; TODO: add env vars that have be shared across BE and FE here
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
  [:html {:lang "en"}
   (gen-header "League page")
   [:body
    [:div {:id "app"}]
    [:script (format "window['config']=%s" (client-side-config request))]
    [:script {:src (cache-buster "/cljs-out/dev-main.js")}]

    [:script "byf.core.init();"]]])
