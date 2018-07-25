(ns elo.api
  (:gen-class)
  (:require [clojure.walk :refer [keywordize-keys]]
            [compojure.core :refer [defroutes GET POST]]
            [elo.db :refer [store]]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [hiccup.form :as forms]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as resp])
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
     [:p "Enter here the results of the game"]
     [:form
      (forms/drop-down {} "Winning Player" ["one" "two"])
      (forms/drop-down {} "Losing Player" ["one" "two"])
      (forms/drop-down {} "Winning Goals" (map str (range 0 10)))
      (forms/drop-down {} "Losing Goals" (map str (range 0 10)))
      (forms/text-field {} "Winning Team")
      (forms/text-field {} "Losing Team")
      (forms/submit-button {} "Submit Result")]]]])

(defn get-rankings
  []
  )

(defn store!
  [request]
  (let [params (-> request :json-params keywordize-keys)]
    (store params)
    {:status 201
     :body "The result was stored correctly"}))

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
  (GET "/" [] (home))
  (POST "/store" request (store! request)))

(def app
  (-> app-routes
      (r-def/wrap-defaults r-def/secure-site-defaults)
      wrap-keyword-params
      wrap-json-params))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
