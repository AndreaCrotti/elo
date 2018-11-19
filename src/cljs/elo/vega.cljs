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
     "description" "Rankings over time"
     "data" {"values" []}
     "mark" {"type" "line"
             "point" {"tooltip" {"content" "data"}}}

     "encoding" {"y" {"field" "ranking"
                      "type" "quantitative"
                      "scale" {"domain" [min-r max-r]}}

                 "x" {"field" "time"
                      "type" "temporal"}}}))
(defn vega-view
  []
  [:div.rankings-over-time
   [:h4 "Rankings Over Time"]
   [:div {:id vega-div-id}]])

(defn component-did-mount
  [values update-fn]
  (fn [comp]
    (let [vega (.getElementById js/document vega-div-id)
          vega-obj (clj->js (rankings-vega-definition values))])))

(defn vega-inner
  []
  (let [values (atom nil)
        update (fn [comp]
                 [])]

    (reagent/create-class
     {:reagent-render vega-view
      :component-did-mount (component-did-mount values update)
      :component-did-update update
      :display-name "Rankings Over Time Inner"})))

(defn vega-outer
  []
  (let [history (rf/subscribe [:elo.league-detail.handlers/rankings-history])]
    (fn []
      [vega-inner @history])))
