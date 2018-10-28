(ns elo.common.views
  (:require [elo.utils :as utils]))

(defn drop-down
  [opts dispatch-key value & {:keys [value-fn display-fn]
                              :or {value-fn identity
                                   display-fn identity}}]

  (into [:select
         {:on-change (utils/set-val dispatch-key) :value (or value "")}]

        (cons [:option "select your option"
               #_{:disabled true
                :value "Select your option"
                :selected true}]
              (for [o opts]
                [:option {:value (value-fn o)} (display-fn o)]))))
