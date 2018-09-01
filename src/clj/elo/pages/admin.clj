(ns elo.pages.admin
  (:require [elo.pages.utils :refer [cache-buster]]
            [elo.pages.header :refer [gen-header]]))

(defn body
  []
  [:html
   (gen-header "Admin")
   [:body
    ]])
