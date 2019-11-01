(ns byf.common.views
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]))

(defn drop-down
  "Wrapper around a select, which allows to pass the dispatch key and
  the value the select should be set to"
  [opts dispatch-key value & {:keys [value-fn display-fn]
                              :or {value-fn identity
                                   display-fn identity}}]

  (into
   [ant/select {:on-change
                #(rf/dispatch [dispatch-key %])
                :value value}]
   (for [o opts]
     [ant/select-option {:key (value-fn o)
                         :value (value-fn o)} (display-fn o)])))

(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])

(defn errors
  []
  (let [error @(rf/subscribe [:failed])]
    (when errors
     [ant/alert {:type "error"
                 :message error}])))

(defn footer
  []
  [ant/layout-footer
   [:a {:href "https://github.com/AndreaCrotti/elo"
        :target "_blank"}
    "Fork me on Github"]])
