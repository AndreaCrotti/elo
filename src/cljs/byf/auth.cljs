(ns byf.auth
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [byf.config :as config]))

(defn authenticate
  []
  [ant/button {:on-click #(rf/dispatch [:sign-in])} "Authenticate"])

(defn logged-out?
  []
  (and (config/value :auth-enabled)
       (nil? @(rf/subscribe [:user]))))
