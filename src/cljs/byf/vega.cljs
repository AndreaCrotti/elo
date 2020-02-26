(ns byf.vega
  (:require [cljsjs.vega]
            [reagent.core :as reagent]))

(def schema-url "https://vega.github.io/schema/vega-lite/v3.0.0-rc6.json")
(def vega-div-id "rankings-over-time__visualization")

(defn rankings-vega-definition
  [history domain]
  {"$schema" schema-url
   "width" 500
   "height" 500
   "description" "Rankings over time"
   "data" {"values" history}
   "mark" {"type" "line"
           "point" {"tooltip" [{"field" "Player" "type" "Nominal"},
                               {"field" "Result" "type" "Nominal"}
                               {"field" "Game" "type" "quantitative"}
                               {"field" "Time" "type" "Temporal"}
                               {"field" "Ranking" "type" "quantitative"}]}}

   "encoding" {"y" {"field" "Ranking"
                    "type" "quantitative"
                    "scale" {"domain" domain}}

               "color" {"field" "Player"
                        "type" "Nominal"}

               "x" {"field" "Time"
                    "type" "temporal"}}})
(defn vega-view
  []
  [:div.rankings-over-time
   [:div {:id vega-div-id
          :class vega-div-id}]])

(defn vega-update
  [comp]
  (let [[history domain] (rest (reagent/argv comp))]
    (js/vegaEmbed (str "#" vega-div-id)
                  (clj->js (rankings-vega-definition history domain)))))

(defn vega-inner
  []
  (reagent/create-class
   {:reagent-render       vega-view
    :component-did-update vega-update
    :component-did-mount  vega-update
    :display-name         "Rankings Over Time Inner"}))
