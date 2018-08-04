(ns elo.api
  (:gen-class)
  (:require [compojure.core :refer [defroutes GET POST]]
            [elo.db :refer [store load-games]]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :as resources]
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
    [:div {:id "app"}]]])

(defn store!
  [{:keys [params]}]
  (store params)
  {:status 201
   :body "The result was stored correctly"})

(defn home
  []
  (resp/content-type
   (resp/response
    (hiccup/html body))

   "text/html"))

(defn games-table
  []
  (into [:table
         [:th
          [:td "Player 1"]
          [:td "Team"]
          [:td "Goals"]
          [:td "Player 2"]
          [:td "Team"]
          [:td "Goals"]]]

        (for [{:keys [p1-name p2-name p1-team p2-team p1-goals p2-goals]}
              (load-games)]

          [:tr
           [:td p1-name]
           [:td p1-team]
           [:td p1-goals]
           [:td p2-name]
           [:td p2-team]
           [:td p2-goals]])))

(defn games
  []
  (resp/content-type
   (resp/response
    (hiccup/html (load-games)))))

(defn- get-port
  []
  (Integer. (or (env :port) default-port)))

(defroutes app-routes
  (GET "/" [] (home))
  (GET "/games" [] (let [games load-games]
                     {:status 200
                      :body games}))

  (POST "/store" request (store! request)))

(def app
  (-> app-routes
      (resources/wrap-resource "public")
      (r-def/wrap-defaults r-def/api-defaults)
      wrap-keyword-params
      wrap-json-params))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
