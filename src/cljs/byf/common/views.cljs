(ns byf.common.views
  (:require [byf.utils :as utils]
            [re-frame.core :as rf]
            [antizer.reagent :as ant]))

(defn drop-down
  "Wrapper around a select, which allows to pass the dispatch key and
  the value the select should be set to"
  [opts dispatch-key value & {:keys [value-fn display-fn caption]
                              :or {value-fn identity
                                   caption ""
                                   display-fn identity}}]

  (into
   [ant/select {:on-change (utils/set-val dispatch-key) :value (or value "")}]
   (cons [ant/select-option {:disabled true :value caption}
          caption]
         (for [o opts]
           [ant/select-option {:value (value-fn o)} (display-fn o)]))))

(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])

(defn errors
  []
  (let [error @(rf/subscribe [:failed])]
    (when errors
      [:div.error "Error = " (str error)])))
