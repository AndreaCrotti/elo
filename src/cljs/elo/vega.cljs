(ns elo.vega
  (:require [cljsjs.vega]))

(def schema-url "https://vega.github.io/schema/vega-lite/v2.json")

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
  [json]
  (js/console.log "passing " (clj->js json)
                  "to init-vega")

  (js/vegaEmbed "#vega-visualization" (clj->js fixed))
  #_(js/vegaEmbed "#vega-visualization" (clj->js json)))
