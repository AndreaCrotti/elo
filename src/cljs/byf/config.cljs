(ns byf.config)

;; TODO: could be moved to the config.cljc with a conditional compilation thing
(defn value
  [k]
  (-> (.-cfg js/window)
      (js->clj :keywordize-keys true)
      k))
