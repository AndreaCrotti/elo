(ns elo.pages.home
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "League page")
   [:body
    #_[:div.plots
       [:div {:id "plot-cljs"} "Space for the plot is here"]
       [:div {:id "plot-js"} "Space for the pure JS plot is here"]]

    [:div {:id "app"}]
    [:script {:src (cache-buster "/cljs-out/elo-main.js")}]
    #_[:script {:src (cache-buster "js/playground.js")}]

    [:script "elo.core.init();"]]])
