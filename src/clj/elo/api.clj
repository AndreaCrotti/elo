(ns elo.api
  (:gen-class)
  (:require [bidi.ring :refer [make-handler]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [elo.auth :refer [basic-auth-backend with-basic-auth oauth2-config]]
            [elo.csv :as csv]
            [elo.db :as db]
            [elo.games :as games]
            [elo.notifications :as notifications]
            [elo.pages.home :as home]
            [elo.validate :as validate]
            [environ.core :refer [env]]
            [hiccup.core :as hiccup]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.oauth2 :refer [wrap-oauth2]]
            [ring.middleware.resource :as resources]
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

(def games-csv-header
  [:p1
   :p1_using
   :p2
   :p2_using
   :played_at])

(defn csv-transform
  [fields rows name-mapping]
  (let [filtered-rows (map #(select-keys % fields) rows)
        to-name #(get name-mapping %)
        transform {:played_at str
                   :p1 to-name
                   :p2 to-name}]

    (map #(vals (reduce-kv update % transform)) filtered-rows)))

(defn csv-body
  [response csv-header csv-rows]
  (assoc response
         :body
         (ring-io/piped-input-stream
          (csv/write-csv csv-header csv-rows))))

(defn games-csv
  [request]
  ;; fetch all the games normalizing the player names if possible as
  ;; part of the process
  (let [league-id (get-league-id request)
        games (db/load-games league-id)
        players (db/load-players league-id)
        names-mapping (games/player->names players)]

    (-> {}
        (csv-body games-csv-header
                  (csv-transform games-csv-header games names-mapping))
        (resp/status 200)
        (resp/content-type "text/csv")
        (resp/header "Content-Disposition" "attachment; filename=\"games.csv\""))))

(defn rankings-csv
  [request]
  ;; return the list of all the rankings per player
  ;; in a form like
  ;; p1, p2, p3
  ;; 1500, 1500, 1500
  ;; 1512, 1488, 1500
  ;; for all the possible games played

  (let [league-id (get-league-id request)
        games (db/load-games league-id)
        players (db/load-players league-id)
        header (map :name players)
        csv-rows (for [n (range (inc (count games)))]
                   (map (comp str :ranking) (games/get-rankings
                                             (take n games)
                                             players)))]

    (-> {}
        (csv-body header csv-rows)
        (resp/status 200)
        (resp/content-type "text/csv")
        (resp/header "Content-Disposition" "attachment; filename=\"rankings.csv\""))))

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
                "rankings-csv" rankings-csv

                "oauth2/github/callback" github-callback}

        ;; quite a crude way to make sure all the other urls actually
        ;; render to the SPA, letting the routing be handled by
        ;; accountant
        ;; TODO: this might be a problem for things like the ring oauth
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
