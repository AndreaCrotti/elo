(ns elo.css
  (:require [garden.def :refer [defstyles defcssfn]]))

(defstyles screen
  ;; could maybe even split creating multiple CSS files?
  [[:.content
    {:display "grid"
     :width "90%"
     :padding-left "20px"
     :padding-top "30px"
     ;; :grid-template-columns "auto"
     ;; :grid-template-rows "auto auto auto"
     :grid-gap "20px"

     }]

   [:.rankings__table
    {:width "300px"}]

   [:.fork-me
    {:position "absolute"
     :top 0
     :right 0
     :border 0}]

   [:.players_form
    {:display "grid"
     :width "80%"
     :padding-left "15px"
     :grid-gap "10px"
     :grid-template-rows "auto auto auto"
     :grid-template-columns "auto auto"}]

   [:label {:padding-right "30px"}]])
