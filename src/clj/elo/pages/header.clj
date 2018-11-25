(ns elo.pages.header
  (:require [elo.config :as config]
            [elo.pages.utils :refer [cache-buster]]
            [elo.pages.common :refer [adsense-js ga-js]]))

(defn- google-font
  [font-name]
  [:link {:rel "stylesheet"
          :href (format "//fonts.googleapis.com/css?family=%s" font-name)}])

(def fonts
  {:titles "Monoton"
   :smaller-titles "Lilita+One"})

(defn cljsjs-css
  [path]
  (let [full-path (format "/cljsjs/%s" path)]
    [:link {:href (cache-buster full-path)
            :rel "stylesheet"
            :type "text/css"}]))

(defn gen-header
  [title]
  [:head [:meta {:charset "utf-8"
                 :description "FIFA championship little helper"}]

   [:title title]

   (google-font (:titles fonts))
   (google-font (:smaller-titles fonts))

   (cljsjs-css "vega_embed.css")
   (cljsjs-css "fontawesome/all.css")
   (cljsjs-css "react-datepicker/production/react-datepicker.min.inc.css")
   (cljsjs-css "bootstrap.css")

   [:link {:href (cache-buster "/css/screen.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/playground.css")
           :rel "stylesheet"
           :type "text/css"}]

   (when config/google-analytics-tag
     [:script {:async true
               :src (format "https://www.googletagmanager.com/gtag/js?id=%s"
                            config/google-analytics-tag)}])

   (when config/google-analytics-tag
     (ga-js))

   (when config/adsense-tag
     [:script {:async true
               :src "//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"}])

   (when config/adsense-tag
     (adsense-js))])
