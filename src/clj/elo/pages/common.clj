(ns elo.pages.common
  (:require [elo.config :refer [value]]))

(defn ga-js
  []
  [:script (format "
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', '%s');" (value :google-analytics-tag))])
