(ns elo.css
  (:require [garden.def :refer [defstyles defcssfn]]
            [garden.core :refer [css]]
            [garden.stylesheet :refer [at-media]]))

(defstyles screen
  ;; could maybe even split creating multiple CSS files?
  [[:.players_form
    {:display "grid"
     :grid-gap "10px"
     :grid-template-rows "auto auto"
     :grid-template-columns "auto auto auto"}]])
