(ns elo.api
  (:gen-class)
  (:require [bidi.ring :refer [make-handler]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [elo.auth :refer [basic-auth-backend with-basic-auth oauth2-config]]
            [elo.db :as db]
            [elo.csv :as csv]
            [elo.notifications :as notifications]
            [elo.pages.home :as home]
            [elo.validate :as validate]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :as resources]
            [ring.middleware.oauth2 :refer [wrap-oauth2]]
            [ring.util.io :as ring-io]
            [ring.util.response :as resp]
            [taoensso.timbre :as timbre :refer [log info debug]])
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
  (notifications/notify-slack "A new game was added!")
  (let [validated (validate/conform :game params)
        game-id (db/add-game! validated)]

    (as-json
     (resp/created "/api/games"
                   {:id game-id}))))

(defn add-player!
  "Adds a new user to the platform, authenticated with basic Auth"
  [{:keys [params] :as request}]
  (notifications/notify-slack "A new player just joined!")
  (with-basic-auth
    request
    (let [validated (validate/conform :player params)
          ids (db/add-player-full! validated)]

      (as-json
       (resp/created "/api/players" ids)))))

(defn- render-page
  [page]
  (resp/content-type
   (resp/response
    (hiccup/html page))

   "text/html"))

(defn spa [_] (render-page (home/body)))

;;TODO: the league_id has to be extracted on all these different handlers

(defn- get-league-id
  [request]
  (-> request
      :params
      :league_id
      validate/to-uuid))

(defn get-players
  [request]
  (-> (get-league-id request)
      db/load-players
      resp/response
      as-json))

(defn get-games
  [request]
  (-> (get-league-id request)
      db/load-games
      resp/response
      as-json))

(defn get-league
  [request]
  (-> (get-league-id request)
      db/load-league
      resp/response
      as-json))

(defn get-leagues
  [request]
  ;;TODO: should get the company-id as argument ideally
  (-> (db/load-leagues)
      resp/response
      as-json))

(defn get-companies
  [request]
  ;;TODO: should get the company-id as argument ideally
  (-> (db/load-companies)
      resp/response
      as-json))

(defn github-callback
  [request]
  {:status 200
   :body "Correctly Went throught the whole process"})

(defn games-csv
  [request]
  ;; fetch all the games normalizing the player names if possible as
  ;; part of the process
  (let [games (db/load-games (get-league-id request))]
    (-> {:status 200
         :body (ring-io/piped-input-stream
                (csv/write-csv ["a"] [[100]]))}

        (resp/content-type "text/csv")
        (resp/header "Content-Disposition" "attachment; filename=\"games.csv\""))))

;;TODO: add a not found page for everything else?
(def routes
  ["/" {"api/" {"add-player" add-player!
                "add-game" add-game!

                "league" get-league
                "leagues" get-leagues
                "companies" get-companies
                "players" get-players
                "games" get-games

                ;; csv stuff
                "games-csv" games-csv

                "oauth2/github/callback" github-callback}

        ;; quite a crude way to make sure all the other urls actually
        ;; render to the SPA, letting the routing be handled by
        ;; accountant
        true spa}])

(def handler
  (make-handler routes))

(defn log-request
  "Simple middleware to log all the requests"
  [handler]
  (fn [request]
    (info request)
    (handler request)))

(defn- enable-cookies
  [params]
  (assoc-in params [:session :cookie-attrs :same-site] :lax))

(def app
  (-> handler
      (resources/wrap-resource "public")
      (r-def/wrap-defaults
       (enable-cookies r-def/api-defaults))

      (wrap-authorization basic-auth-backend)
      (wrap-authentication basic-auth-backend)
      wrap-keyword-params
      wrap-json-params
      wrap-json-response
      (wrap-oauth2 oauth2-config)
      log-request))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
