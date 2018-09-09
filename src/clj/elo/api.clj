(ns elo.api
  (:gen-class)
  (:require [bidi.ring :refer [make-handler]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [elo.auth :refer [basic-auth-backend with-basic-auth oauth2-config]]
            [elo.db :as db]
            [elo.pages.home :as home]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :as resources]
            [ring.middleware.oauth2 :refer [wrap-oauth2]]
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

(defn spa [_] (render-page (home/body)))

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

(defn get-league
  [req]
  (as-json
   (resp/response (db/load-league (get-league-id req)))))

(defn get-leagues
  [req]
  ;;TODO: should get the company-id as argument ideally
  (as-json
   (resp/response (db/load-leagues))))

(defn github-callback
  [request]
  {:status 200
   :body "Correctly Went throught the whole process"})

;;TODO: add a not found page for everything else?
(def routes
  ["/" {"api/" {
                "add-player" add-player!
                "add-game" add-game!

                "league" get-league
                "leagues" get-leagues
                "players" get-players
                "games" get-games

                "oauth2/github/callback" github-callback}

        ;; quite a crude way to make sure all the other urls actually
        ;; render to the SPA, letting the routing be handled by
        ;; accountant
        true spa}])

(def handler
  (make-handler routes))

(defn log-request
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
