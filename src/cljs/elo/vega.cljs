(ns elo.vega
  (:require [cljsjs.vega]
            [reagent.core :as reagent]))

(def schema-url "https://vega.github.io/schema/vega-lite/v3.0.0-rc6.json")

(defn vega-view
  []
  [:div.rankings-over-time
   [:h4 "Rankings Over Time"]
   [:div.rankings-over-time__visualization {:style {:height "400px"}}]])

(defn vega-inner
  []
  (let [values (atom nil)
        update (fn [comp]
                 [])]
    (reagent/create-class
     {:reagent-render vega-view
      :component-did-mount 1
      :display-name "Rankings Over Time Inner"})))

(defn rankings-vega-definition
  [values]
  {"$schema" schema-url
   "description" "Rankings over time"
   "data" {"values" []}
   "mark" {"type" "line"
           "point" {"tooltip" {"content" "data"}}}

   "encoding" {"y" {"field" "ranking"
                    "type" "quantitative"
                    "scale" {"domain" [1300 1600]}}

               "x" {"field" "time"
                    "type" "temporal"}}})

(defn init-vega
  [data]
  (js/console.log "Got data to visualize = " data)
  (js/vegaEmbed "#vega-visualization"
                (clj->js (rankings-vega-definition data))))
