(ns elo.api
  (:gen-class)
  (:require [compojure.core :refer [defroutes GET POST]]
            [elo.db :refer [store load-games]]
            [elo.core :as core]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :as resources]
            [ring.util.response :as resp])
  (:import (java.util UUID)))

(def ^:private default-port 3000)

(defn- get-port
  []
  (Integer. (or (env :port) default-port)))

(defn- cache-buster
  [path]
  ;; fallback to a random git sha when nothing is found
  (format "%s?git_sha=%s"
          path
          (:heroku-slug-commit env (str (UUID/randomUUID)))))

(defn- as-json
  [response]
  (resp/content-type response "application/json"))

(def body
  [:html
   [:head [:meta {:charset "utf-8"
                  :description "FIFA championship little helper"}]
    [:title "FIFA championship"]

    [:link {:rel "stylesheet"
            :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"
            :integrity "sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm"
            :crossorigin "anonymous"}]

    [:link {:href (cache-buster "css/screen.css")
            :rel "stylesheet"
            :type "text/css"}]]

   [:body
    [:div {:id "app"}]
    [:script {:src (cache-buster "js/compiled/app.js")}]
    [:script "elo.core.init();"]]])

(defn store!
  [{:keys [params]}]
  (as-json
   (let [result (store params)]
     {:status 201
      :body result})))

(defn home
  []
  (resp/content-type
   (resp/response
    (hiccup/html body))

   "text/html"))

(defn games
  []
  (as-json
   (resp/response (reverse (load-games)))))

(defn get-rankings
  []
  (as-json
   (resp/response
    (let [games (load-games)
          norm-games (map core/normalize-game games)]
      (core/compute-rankings norm-games)))))

(defroutes app-routes
  (GET "/" [] (home))
  (GET "/games" [] (let [games (load-games)]
                     {:status 200
                      :body games}))

  (GET "/rankings" [] (get-rankings))
  (POST "/store" request (store! request)))

(def app
  (-> app-routes
      (resources/wrap-resource "public")
      (r-def/wrap-defaults r-def/api-defaults)
      wrap-keyword-params
      wrap-json-params
      wrap-json-response))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
