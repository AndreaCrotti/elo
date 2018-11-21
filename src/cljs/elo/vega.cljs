(ns elo.vega
  (:require [cljsjs.vega]
            [re-frame.core :as rf]
            [reagent.core :as reagent]))

(def schema-url "https://vega.github.io/schema/vega-lite/v3.0.0-rc6.json")
(def vega-div-id "rankings-over-time__visualization")

(defn rankings-vega-definition
  [values]
  (let [min-r (apply min (map #(get % "Ranking") values))
        max-r (apply max (map #(get % "Ranking") values))]

    {"$schema" schema-url
     "width" 500
     "height" 500
     "description" "Rankings over time"
     "data" {"values" values}
     "mark" {"type" "line"
             "point" {"tooltip" {"content" "data"}}}

     "encoding" {"y" {"field" "Ranking"
                      "type" "quantitative"
                      "scale" {"domain" [min-r max-r]}}

                 "color" {"field" "Player"
                          "type" "Nominal"}

                 "x" {"field" "Time"
                      "type" "temporal"}}}))
(defn vega-view
  []
  [:div.rankings-over-time
   [:div {:id vega-div-id
          :class vega-div-id}]])

(defn vega-inner
  []
  (let [update (fn [comp]
                 (let [data (second (reagent/argv comp))]
                   (js/vegaEmbed (str "#" vega-div-id)
                                 (clj->js (rankings-vega-definition data)))))]

    (reagent/create-class
     {:reagent-render vega-view
      :component-did-update update
      :display-name "Rankings Over Time Inner"})))

(defn vega-outer
  []
  (let [history (rf/subscribe [:elo.league-detail.handlers/rankings-history])]
    (fn []
      [vega-inner @history])))
