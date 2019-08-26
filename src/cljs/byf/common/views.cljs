(ns byf.common.views
  (:require [byf.utils :as utils]
            [re-frame.core :as rf]))

(defn drop-down
  "Wrapper around a select, which allows to pass the dispatch key and
  the value the select should be set to"
  [opts dispatch-key value & {:keys [value-fn display-fn caption]
                              :or {value-fn identity
                                   caption ""
                                   display-fn identity}}]

  [:div.select.is-fullwidth
   (into [:select.select
          {:on-change (utils/set-val dispatch-key) :value (or value "")}]

         (cons [:option {:disabled true
                         :value caption}
                caption]
               (for [o opts]
                 [:option {:value (value-fn o)} (display-fn o)])))])

(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])

(defn errors
  []
  (let [error @(rf/subscribe [:failed])]
    (when errors
      [:div.error "Error = " error])))
