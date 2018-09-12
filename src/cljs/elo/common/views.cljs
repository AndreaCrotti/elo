(ns elo.common.views
  (:require [elo.utils :as utils]))

(defn drop-down
  [opts dispatch-key & {:keys [value-fn display-fn]
               :or {value-fn identity
                    display-fn identity}}]

  (into [:select.form-control {:on-change (utils/set-val dispatch-key) :value ""}]
        (cons [:option ""]
              (for [o opts]
                [:option {:value (value-fn o)} (display-fn o)]))))
