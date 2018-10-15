(ns elo.pages.header
  (:require [elo.config :as config]
            [elo.pages.utils :refer [cache-buster]]
            [elo.pages.common :refer [adsense-js ga-js]]))

(defn gen-header
  [title]
  [:head [:meta {:charset "utf-8"
                 :description "FIFA championship little helper"}]

   [:title title]

   ;; should we get different packages?
   [:link {:rel "stylesheet"
           :href "https://use.fontawesome.com/releases/v5.3.1/css/all.css"
           :integrity "sha384-mzrmE5qonljUremFsqc01SB46JvROS7bZs3IO2EmfFsd15uHvIt+Y8vEf7N7fWAU"
           :crossorigin "anonymous"}]

   [:link {:href (cache-buster "/css/react-datepicker.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:script {:src "//cdn.plot.ly/plotly-latest.min.js"}]

   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
           :integrity "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
           :crossorigin "anonymous"}]

   [:link {:href (cache-buster "/css/bootstrap-social.css")
           :rel "stylesheet"
           :type "text/css"}]

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
