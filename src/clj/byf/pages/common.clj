(ns byf.pages.common
  (:require [byf.config :refer [value]]))

(defn ga-js
  []
  [:script (format "
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());

  gtag('config', '%s');" (value :google-analytics-tag))])
