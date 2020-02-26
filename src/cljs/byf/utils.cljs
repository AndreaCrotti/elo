(ns byf.utils
  "Various utility functions for Clojurescript"
  (:require [clojure.string :as str]
            [goog.object :as object]
            [re-frame.core :as rf]))

(def min-width 500)

(defn classes
  [cls]
  (str/join " "
            (map name
                 (filter some? cls))))

(defn set-val
  ([handler-key transform-fn]
   #(rf/dispatch [handler-key (-> % .-target .-value transform-fn)]))

  ([handler-key]
   (set-val handler-key identity)))

(defn mobile?
  []
  (< js/window.screen.availWidth min-width))

(defn comparator-by
  "Compares two objects based on field"
  [field]
  #(compare (object/get %1 (name field))
            (object/get %2 (name field))))
