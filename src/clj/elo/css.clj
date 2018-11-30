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
  [[:.section {:max-width "1000px"
               :margin "0 auto"}]

   [:.rankings__alive {:display "flex"}]

   [:.result__element
    {:color "white"
     :margin "2px"
     :font-weight :bolder}]

   [:.up-to-range-slider
    {:height "15px"
     :border-radius "5px"
     :background (-> color-palette :aggressive :third)
     :outline "none"
     :opacity "0.6"
     :-webkit-transition ".2s"
     :transition "opacity .2s"}]

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

   [:.highest__ranking__name {:padding-right "5px"}]
   [:.highest__ranking__points {:padding-right "5px"}]
   [:.longest__name {:padding-right "5px"}]

   [:.section {:padding "10px"}]])

(defstyles screen
  ;; could maybe even split creating multiple CSS files?
  (concat auth
          leagues-page
          league-detail-page))
