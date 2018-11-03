(ns elo.css
  (:require [garden.def :refer [defstyles defcssfn]]))

(def internal-grid-gap "10px")
(def grid-gap "15px")

(def font-families
  {:lilita "'Lilita One', cursive"
   :monoton "'Monoton', cursive"})

(def color-palette
  {:aggressive {:main "#FF4136"
                :secondary "#001f3f"
                :third "#85144b"}})

(def leagues-page
  [[:.league_list__root {:display "grid"
                         :width "80%"
                         :grid-gap "30px"
                         :padding-top "10px"
                         :padding-left "10px"}

    [:.sign-up__block {:width "200px"
                       :justify-self "center"}]]

   [:.language_pick {:font-size "24px"
                     :text-align "center"}]])

(def auth
  [[:.auth__root {:align-self "center"
                  :justify-content "center"
                  :padding-top "50px"
                  :display "flex"}]])

(def league-detail-page
  [[:.vega-visualization
    {:width "600px"
     :height "500px"}]

   [:h2 {:font-family (:monoton font-families)}]
   [:label {:display "block"}]

   [:select {:background-color "white"
             :border-size "2px"}]

   [:.league_detail__root
    {:width "90%"
     :padding-left "20px"
     :padding-top "30px"}]

   [:.rankings__alive {:display "flex"}]

   [:.change__status
    {:padding-right "5px"
     :padding-left "5px"
     :margin-right "8px"}]

   [:.result__element
    {:color "white"
     :margin "2px"
     :font-weight :bolder}]

   [:.rankings-slider
    {:padding "20px"}]

   [:.result__w
    {:background-color "green"}]

   [:.result__d
    {:background-color "black"}]

   [:.result__l
    {:background-color "red"}]

   [:th {:text-transform "uppercase"}]
   [:.fas {:font-size "20px"
           :cursor "pointer"}]

   [:.up-to-current-games {:font-size "20px"
                           :padding-left "5px"
                           :padding-right "5px"
                           :background-color "#DDDDDD"}]

   [:.navbar__container
    {:display "flex"
     :justify-content "space-between"
     :width "80%"}]

   [:.form__row
    {:display "flex"
     :flex-wrap "wrap"}]

   [:label {:padding-right "30px"}]

   [:.section {:padding "10px"
               :box-shadow "-1px 1px 2px 2px rgba(0,0,0,0.2)"}]])

;;TODO: add some media wrapping with media magic
;;and flexbox
;; @media all and (max-width: 600px) {
  
;;   .container {
;;     flex-wrap: wrap;
;;   }
  
;;   .container > li {
;;     flex-basis: 50%;
;;   }
;; }

(defstyles screen
  ;; could maybe even split creating multiple CSS files?
  (concat auth
          leagues-page
          league-detail-page))
