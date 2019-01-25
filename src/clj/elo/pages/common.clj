(ns elo.pages.common
  (:require [elo.config :as config]))

(defn ga-js
  []
  [:script (format "
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', '%s');" config/google-analytics-tag)])
