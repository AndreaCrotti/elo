(ns byf.league-detail.utils)

(defn format-date
  [timestamp]
  (.format (js/moment timestamp) "YYYY-MM-DD"))
