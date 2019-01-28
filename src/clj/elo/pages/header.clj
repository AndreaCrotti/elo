(ns elo.pages.header
  (:require [elo.config :refer [config]]
            [elo.pages.utils :refer [cache-buster]]
            [elo.pages.common :refer [ga-js]]))

(defn- google-font
  [font-name]
  [:link {:rel "stylesheet"
          :href (format "//fonts.googleapis.com/css?family=%s" font-name)}])

(def fonts
  {:titles "Monoton"
   :smaller-titles "Lilita+One"})

(defn gen-header
  [title]
  [:head [:meta {:charset "utf-8"
                 :description "FIFA championship little helper"}]

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

   (when (:google-analytics-tag config)
     [:script {:async true
               :src (format "https://www.googletagmanager.com/gtag/js?id=%s"
                            (:google-analytics-tag config))}])

   (when (:google-analytics-tag config)
     (ga-js))])
