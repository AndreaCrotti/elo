(ns elo.api
  (:gen-class)
  (:require [ring.util.response :as resp]
            [ring.middleware.defaults :as r-def]
            [environ.core :refer [env]]
            [compojure.core :refer [defroutes GET POST]]
            [elo.db :refer [store]]
            [ring.adapter.jetty :as jetty]
            [hiccup.core :as hiccup])
  (:import (java.util UUID)))

(def ^:private default-port 3000)

(defn- cache-buster
  [path]
  ;; fallback to a random git sha when nothing is found
  (format "%s?git_sha=%s"
          path
          (:heroku-slug-commit env (str (UUID/randomUUID)))))


(def body
  [:html
   [:head [:meta {:charset "utf-8"
                  :description "FIFA championship little helper"}]
    [:title "FIFA championship"]

    [:link {:href (cache-buster "css/screen.css")
            :rel "stylesheet"
            :type "text/css"}]]

   [:body
    [:div {:id "root"}
     [:p "Enter here the results of the game"]]]])

(defn home
  []
  (resp/content-type
   (resp/response
    (hiccup/html body))

   "text/html"))

(defn- get-port
  []
  (Integer. (or (env :port) default-port)))

(defroutes app-routes
  (POST "/store" request ())
  (GET "/" [] (home))
  #_(GET "/" request (enter-page request)))

(def app
  (-> app-routes
      (r-def/wrap-defaults {})))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
