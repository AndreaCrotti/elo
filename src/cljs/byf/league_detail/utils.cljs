(ns byf.league-detail.utils
  (:require [lambdaisland.uri :refer [uri]]))

(defn format-date
  [timestamp]
  (.format (js/moment timestamp) "YYYY-MM-DD"))

(defn enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))


(defn update-fragment
  [url new-fragment]
  (-> (uri url)
      (assoc :fragment new-fragment)
      uri
      str))
