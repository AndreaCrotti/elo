(ns elo.pages.common
  (:require [elo.config :as config]))

(defn ga-js
  []
  [:script (format "
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', '%s');" config/google-analytics-tag)])

(defn adsense-js
  []
  [:script (format
            "(adsbygoogle = window.adsbygoogle || []).push({
              google_ad_client: '%s',
              enable_page_level_ads: true
  });
"
            config/adsense-tag)])
