(ns byf.elements
  (:require [byf.utils :refer [classes]]))

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
