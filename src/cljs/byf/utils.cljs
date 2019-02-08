(ns byf.utils
  (:require [clojure.string :as str]
            [re-frame.core :as rf]))

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
