(ns elo.api
  (:gen-class)
  (:require [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [bidi.ring :refer [make-handler]]
            [elo.auth :refer [basic-auth-backend with-basic-auth]]
            [elo.core :as core]
            [elo.db :as db]
            [elo.pages.home :as home]
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
  (let [player-id (db/add-player! params)]
    (with-basic-auth request
      (as-json
       (resp/created "/players"
                     {:id player-id})))))

(defn home
  [_]
  (resp/content-type
   (resp/response
    (hiccup/html home/body))

   "text/html"))

(defn player->ngames
  [games]
  (frequencies
   (flatten
    (for [g games]
      ((juxt :p1 :p2) g)))))

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

(defn get-rankings
  "Return all the rankings"
  [req]
  ;; (assert false)
  (as-json
   (resp/response
    (let [league-id (get-league-id req)
          games (db/load-games league-id)
          norm-games (map core/normalize-game games)
          players (db/load-players league-id)
          rankings (core/compute-rankings norm-games (map :id players))
          ngames (player->ngames games)]

      (reverse
       (sort-by :ranking
                (for [[k v] rankings]
                  {:id k :ranking v :ngames (get ngames k 0)})))))))

(defn get-players
  [req]
  (as-json
   (resp/response (db/load-players (get-league-id req)))))

(defn get-games
  [req]
  (as-json
   (resp/response (db/load-games (get-league-id req)))))

;;TODO: add a not found page for everything else?
(def routes
  ["/" {
        ;; "company/" {"" :companies
        ;;             [:company-id] ::company}

        ;; "league" {"" :leagues
        ;;           [:league-id] ::league}

        ;;TODO: try to make this more restful
        "" home

        "add-player" add-player!
        "add-game" add-game!

        "players" get-players
        "rankings" get-rankings
        "games" get-games}])

(def handler
  (make-handler routes))

(def app
  (-> handler
      (resources/wrap-resource "public")
      (r-def/wrap-defaults r-def/api-defaults)
      (wrap-authorization basic-auth-backend)
      (wrap-authentication basic-auth-backend)
      wrap-keyword-params
      wrap-json-params
      wrap-json-response))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
