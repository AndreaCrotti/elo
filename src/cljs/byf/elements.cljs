(ns byf.elements
  (:require [byf.utils :refer [classes]]))

(defn el
  [tag base-class]
  (fn [cls args body]
    [tag
     (merge args
            {:class (classes (conj cls base-class))})
     (when (some? body)
       body)]))

(defn input
  [cls args]
  [:input
   (merge args
          {:class (classes (conj cls :input))})])

(defn button
  [cls args body]
  [:button
   (merge args
          (:class (classes (conj cls :button))))
   body])
