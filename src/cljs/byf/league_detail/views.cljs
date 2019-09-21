(ns byf.league-detail.views
  (:require [byf.league-detail.mobile :as mobile]
            [byf.league-detail.desktop :as desktop]))

(defn mobile?
  []
  (< js/window.screen.availWidth 500))

(defn root
  []
  ;; this is kind of an antipattern for reframe
  (if (mobile?)
    [desktop/root]
    [desktop/root]))
