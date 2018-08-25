(ns elo.css
  (:require [garden.def :refer [defstyles defcssfn]]))

(def internal-grid-gap "10px")
(def grid-gap "15px")

(defstyles screen
  ;; could maybe even split creating multiple CSS files?
  [[:.content
    {:display "grid"
     :width "90%"
     :padding-left "20px"
     :padding-top "30px"
     :grid-gap grid-gap}]

   [:.fork-me
    {:position "absolute"
     :top 0
     :right 0
     :border 0}]

   [:.add-player_form
    {:display "grid"
     :width "70%"
     :padding-left "15px"
     :grid-gap internal-grid-gap}]

   [:.game_form
    {:display "grid"
     :width "80%"
     :padding-left "15px"
     :grid-gap internal-grid-gap
     :grid-template-rows "auto auto auto auto"
     :grid-template-columns "auto auto"}]

   [:label {:padding-right "30px"}]

   [:.section {:padding "10px"
               :box-shadow "-1px 1px 2px 2px rgba(0,0,0,0.2)"}]])
