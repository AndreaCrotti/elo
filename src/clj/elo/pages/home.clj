(ns elo.pages.home
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]))

(def klipse-css-href "https://storage.googleapis.com/app.klipse.tech/css/codemirror.css")

(def klipse-selector
  "window.klipse_settings = {
        selector: '.language-klipse'
    };")

(def klipse-plugin-href "https://storage.googleapis.com/app.klipse.tech/plugin/js/klipse_plugin.js")

(defn body
  []
  [:html
   (gen-header "League page")
   [:link {:rel "stylesheet" :type "text/css" :href klipse-css-href}]
   [:body
    [:div {:id "app"}]
    [:script {:src (cache-buster "/cljs-out/elo-main.js")}]
    #_[:script {:src (cache-buster "js/playground.js")}]

    [:script klipse-selector]
    [:script {:src klipse-plugin-href}]
    [:script "elo.core.init();"]]])
