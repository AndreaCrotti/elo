(ns elo.vega
  (:require [cljsjs.vega :as v]))

(def schema-url "https://vega.github.io/schema/vega-lite/v2.json")

(def sample-data
  {"$schema" schema-url
   "data" {"values" [{"a" "C" "b" 2}
                     {"a" "C" "b" 7}
                     {"a" "C" "b" 4}]}
   "mark" "bar"
   "encoding" {"y" {"field" "a"
                    "type" "nominal"}
               "x" {"aggregate" "average"
                    "field" "b"
                    "type" "quantitative"
                    "axis" {"title" "Average of b"}}}})

(v/vegaEmbed "#vega-visualization" sample-data)
