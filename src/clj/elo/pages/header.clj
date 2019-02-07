(ns elo.pages.header
  (:require [clojure.data.json :as json]
            [elo.config :refer [value config]]
            [elo.pages.utils :refer [cache-buster]]
            [elo.pages.common :refer [ga-js]]))

(defn- google-font
  [font-name]
  [:link {:rel "stylesheet"
          :href (format "//fonts.googleapis.com/css?family=%s" font-name)}])

(def fonts
  {:titles "Monoton"
   :smaller-titles "Lilita+One"})

(defn global-client-side-config
  []
  (-> config
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

   ;; should we get different packages?
   [:link {:rel "stylesheet"
           :href "https://use.fontawesome.com/releases/v5.3.1/css/all.css"
           :integrity "sha384-mzrmE5qonljUremFsqc01SB46JvROS7bZs3IO2EmfFsd15uHvIt+Y8vEf7N7fWAU"
           :crossorigin "anonymous"}]

   [:script {:src "https://cdn.jsdelivr.net/npm/vega@4.2.0"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@3.0.0-rc6"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@3.19.2"}]

   [:link {:href (cache-buster "/css/vega_embed.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/react-datepicker.css")
           :rel "stylesheet"
           :type "text/css"}]
   [:link {:href (cache-buster "/css/bootstrap-social.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/screen.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:href (cache-buster "/css/playground.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
           :integrity "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
           :crossorigin "anonymous"}]

   (when (value :google-analytics-tag)
     [:script {:async true
               :src (format "https://www.googletagmanager.com/gtag/js?id=%s"
                            (value :google-analytics-tag))}])

   (when (value :google-analytics-tag)
     (ga-js))])
