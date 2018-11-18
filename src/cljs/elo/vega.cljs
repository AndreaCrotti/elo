(ns elo.vega
  (:require [cljsjs.vega]
            [reagent.core :as reagent]))

(def schema-url "https://vega.github.io/schema/vega-lite/v2.json")

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
   "data" {"values" values}
   "mark" {"type" "line"
           "point" {"tooltip" {"content" "data"}}}

   "encoding" {"y" {"field" "ranking"
                    "type" "quantitative"}

               "x" {"field" "time"
                    "type" "temporal"}}})

(def fixed
  [{"ranking" 1400
    "time" "2018-01-04T12:05:48.000000000-00:00"}

   {"ranking" 1500
    "time" "2018-01-04T12:05:48.000000000-00:00"}])

(defn init-vega
  []
  (js/vegaEmbed "#vega-visualization"
                (clj->js (rankings-vega-definition fixed))))
