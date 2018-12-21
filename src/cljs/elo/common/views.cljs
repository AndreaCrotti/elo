(ns elo.common.views
  (:require [elo.utils :as utils]))

(defn drop-down
  [opts dispatch-key value & {:keys [value-fn display-fn caption]
                              :or {value-fn identity
                                   caption ""
                                   display-fn identity}}]

  (into [:select.form-control
         {:on-change (utils/set-val dispatch-key) :value (or value "")}]

        (cons [:option {:disabled true
                        :value caption}
               caption]
              (for [o opts]
                [:option {:value (value-fn o)} (display-fn o)]))))


(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])
