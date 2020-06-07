(ns byf.auth
  (:require [byf.config :as config]
            [re-frame.core :as rf]))

(defn authenticate
  []
  [:a {:on-click #(rf/dispatch [:sign-in])}
   [:img {:src "/login.png" :alt "login"}]])

(defn logged-out?
  []
  (and (config/value :auth-enabled)
       (nil? @(rf/subscribe [:user]))))
