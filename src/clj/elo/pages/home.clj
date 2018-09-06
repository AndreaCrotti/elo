(ns elo.pages.home
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "League page")
   [:body
    [:div.plots
     [:div {:id "plot-cljs"} "Space for the plot is here"]
     [:div {:id "plot-js"} "Space for the pure JS plot is here"]]

    #_[:div {:id "app"}]
    #_[:script {:src (cache-buster "js/compiled/app.js")}]
    [:script {:src (cache-buster "js/playground.js")}]

    #_[:script "elo.core.init();"]]])
