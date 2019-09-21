(ns byf.league-detail.utils)

(defn format-date
  [timestamp]
  (.format (js/moment timestamp) "YYYY-MM-DD"))

(defn enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))
