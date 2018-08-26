(ns elo.dropdown
  (:require [cljsjs.react-select]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

(def sample-options (atom ["one"  "two" "three"]))

(defn select
  "Select based on a atom/cursor. Pass as state"
  [{:keys [state] :as props}]
  [:> js/Select.Async
   (-> props
       (dissoc state)
       (assoc :value @state
              :on-change (fn [x]
                           (reset! state (some-> x .-value)))))])

(defonce !state (atom nil))

(defn load-options
  [input cb]
  (let [players @(rf/subscribe [:players])]
    (cb nil #js{:options (->> players
                              (map (fn [item]
                                     {:value (:id item)
                                      :label (:name item)}))
                              clj->js)
                :complete true})))
