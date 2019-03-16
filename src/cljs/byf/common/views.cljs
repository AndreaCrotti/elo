(ns byf.common.views
  (:require [byf.utils :as utils]))

(defn drop-down
  [opts dispatch-key value & {:keys [value-fn display-fn caption]
                              :or {value-fn identity
                                   caption ""
                                   display-fn identity}}]

  [:div.select.is-fullwidth
   (into [:select.select.is-large
          {:on-change (utils/set-val dispatch-key) :value (or value "")}]

         (cons [:option {:disabled true
                         :value caption}
                caption]
               (for [o opts]
                 [:option {:value (value-fn o)} (display-fn o)])))])

(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])
