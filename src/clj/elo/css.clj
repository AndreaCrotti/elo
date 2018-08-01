(ns elo.css
  (:require [garden.def :refer [defstyles defcssfn]]
            [garden.core :refer [css]]
            [garden.stylesheet :refer [at-media]]))

(defstyles screen
  ;; could maybe even split creating multiple CSS files?
  [[:.players_form
    {:display "grid"
     :width "80%"
     :padding-left "15px"
     :grid-gap "10px"
     :grid-template-rows "auto auto auto"
     :grid-template-columns "auto auto"}]

   [:label {:padding-right "10px"}]])
