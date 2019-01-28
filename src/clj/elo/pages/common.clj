(ns elo.pages.common
  (:require [elo.config :refer [config]]))

(defn ga-js
  []
  [:script (format "
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', '%s');" (:google-analytics-tag config))])
