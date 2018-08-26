(ns elo.pages.home
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "League page")
   [:body
    [:div {:id "app"}]
    [:script {:src (cache-buster "js/compiled/app.js")}]
    [:script "elo.core.init();"]]])
