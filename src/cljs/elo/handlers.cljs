(ns elo.handlers
  (:require [re-frame.core :as rf]))

(def default-db
  {:games []
   :rankings []})

(rf/reg-event-db :initialize-db
                 (fn [_ _]
                   default-db))
