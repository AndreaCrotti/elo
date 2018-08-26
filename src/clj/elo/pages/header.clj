(ns elo.pages.header
  (:require [elo.config :as config]
            [elo.pages.utils :refer [cache-buster]]
            [elo.pages.common :refer [adsense-js ga-js]]))

(defn gen-header
  [title]
  [:head [:meta {:charset "utf-8"
                 :description "FIFA championship little helper"}]

   [:title title]

   [:link {:href (cache-buster "css/react-datepicker.css")
           :rel "stylesheet"
           :type "text/css"}]

   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
           :integrity "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
           :crossorigin "anonymous"}]

   [:link {:href (cache-buster "css/screen.css")
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
     (adsense-js))

   ;; [:script {:src "https://code.jquery.com/jquery-3.2.1.slim.min.js"
   ;;           :integrity "sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN"
   ;;           :crossorigin "anonymous"
   ;;           :async true}]

   ;; [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"
   ;;           :integrity "sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q"
   ;;           :crossorigin "anonymous"
   ;;           :async true}]

   ;; [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"
   ;;           :integrity "sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl"
   ;;           :crossorigin "anonymous"
   ;;           :async true}]
])
