(ns byf.pages.header
  (:require [clojure.data.json :as json]
            [byf.config :refer [value load-config]]
            [byf.pages.utils :refer [cache-buster]]
            [byf.pages.common :refer [ga-js]]))

(defn- google-font
  [font-name]
  [:link {:rel "stylesheet"
          :href (format "//fonts.googleapis.com/css?family=%s" font-name)}])

(def fonts
  {:titles "Monoton"
   :smaller-titles "Lilita+One"})

(defn global-client-side-config
  []
  (-> (load-config)
      (select-keys [:newrelic-license-key :newrelic-application-id])
      (json/write-str)))

(defn gen-header
  [title]
  [:head [:meta {:charset "utf-8"
                 :description "FIFA championship little helper"}]

   [:script (format "window['cfg']=%s" (global-client-side-config))]

   [:script {:src "/js/newrelic.js"}]

   [:title title]

   (google-font (:titles fonts))
   (google-font (:smaller-titles fonts))

   [:link {:rel "stylesheet"
           :href "/cljsjs/antd/production/antd.min.inc.css"}]
   ;; should we get different packages?

   [:script {:src "https://cdn.jsdelivr.net/npm/vega@4.2.0"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@3.0.0-rc6"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@3.19.2"}]

   [:link {:href (cache-buster "/css/vega_embed.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/tweaks.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/spinner.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/slider.css")
           :rel "stylesheet"
           :type "text/css"}]

   (when (value :google-analytics-tag)
     [:script {:async true
               :src (format "https://www.googletagmanager.com/gtag/js?id=%s"
                            (value :google-analytics-tag))}])

   (when (value :google-analytics-tag)
     (ga-js))])
