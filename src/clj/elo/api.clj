(ns elo.api
  (:gen-class)
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [bidi.ring :refer [make-handler]]
            [elo.auth :refer [basic-auth-backend with-basic-auth]]
            [elo.db :as db]
            [elo.pages.home :as home]
            [elo.pages.leagues :as leagues]
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

(defn- get-port
  []
  (Integer. (or (env :port) default-port)))

(defn- as-json
  [response]
  (resp/content-type response "application/json"))

(defn add-game!
  [{:keys [params]}]
  (let [game-id (db/add-game! params)]
    (as-json
     (resp/created "/games"
                   {:id game-id}))))

(defn add-player!
  "Adds a new user to the platform, authenticated with basic Auth"
  [{:keys [params] :as request}]
  (with-basic-auth
    request
    (let [player-id (db/add-player! params)]
      (as-json
       (resp/created "/players"
                     {:id player-id})))))

(defn- render-page
  [page]
  (resp/content-type
   (resp/response
    (hiccup/html page))

   "text/html"))

(defn home [_] (render-page (home/body)))

(defn leagues [_] (render-page (leagues/body)))

;;TODO: the league_id has to be extracted on all these different handlers

(defn to-uuid
  [v]
  (UUID/fromString v))

(defn- get-league-id
  [request]
  (-> request
      :params
      :league_id
      to-uuid))

(defn get-players
  [req]
  (as-json
   (resp/response (db/load-players (get-league-id req)))))

(defn get-games
  [req]
  (as-json
   (resp/response (db/load-games (get-league-id req)))))

(defn dispatch-home
  [request]
  (if (some? (:query-string request))
    (home request)
    (leagues request)))

;;TODO: add a not found page for everything else?
(def routes
  ["/" {;; "company/" {"" :companies
        ;;             [:company-id] ::company}

        ;; "league" {"" :leagues
        ;;           [:league-id] ::league}

        ;;TODO: try to make this more restful

        "" dispatch-home
        ;;TODO: should actually use this instead of query arguments
        ;; "" leagues

        ;; ["league/" :league-id] home

        "add-player" add-player!
        "add-game" add-game!

        "players" get-players
        "games" get-games}])

(def handler
  (make-handler routes))

(defn update-req
  [request]
  (update request
          :uri
          #(if (or
                (clojure.string/includes? % "js/")
                (clojure.string/includes? % "css/"))

             (subs % 6)
             (identity %))))

(defn rewrite-resources-url
  [handler]
  (fn
    ([request]
     (let [new-req (update-req request)]
       (handler new-req)))))

(def app
  (-> handler
      (resources/wrap-resource "public")
      (r-def/wrap-defaults r-def/api-defaults)
      (wrap-authorization basic-auth-backend)
      (wrap-authentication basic-auth-backend)
      wrap-keyword-params
      wrap-json-params
      wrap-json-response
      #_rewrite-resources-url))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
