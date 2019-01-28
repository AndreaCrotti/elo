(ns elo.api
  (:gen-class)
  (:require [bidi.ring :refer [make-handler]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [clojure.string :as str]
            [elo.auth :refer [basic-auth-backend with-basic-auth oauth2-config]]
            [elo.config :refer [config]]
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
            [ring.util.response]
            [ring.util.http-response :as resp]
            [taoensso.timbre :as timbre :refer [log info debug]])
  (:import (java.util UUID)))

(def ^:private default-port 3000)

(def github-token-path [:oauth2/access-tokens :github :token])

(defn- get-port
  []
  (Integer. (or (env :port) default-port)))

(defn- as-json
  [response]
  (resp/content-type response "application/json"))

(defn add-game!
  [{:keys [params]}]
  (notifications/notify-slack "A new game was added!")
  (let [validated (validate/conform-data :game params)
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
    (let [validated (validate/conform-data :player params)
          ids (db/add-player-full! validated)]

      (as-json
       (resp/created "/api/players" ids)))))

(defn- render-page
  [page]
  (resp/content-type
   (resp/ok
    (hiccup/html page))

   "text/html"))

(defn spa [request] (render-page (home/body request)))

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
      resp/ok
      as-json))

(defn get-games
  [request]
  (-> (get-league-id request)
      db/load-games
      resp/ok
      as-json))

(defn get-league
  [request]
  (-> (get-league-id request)
      db/load-league
      resp/ok
      as-json))

(defn get-leagues
  [request]
  ;;TODO: should get the company-id as argument ideally
  (-> (db/load-leagues)
      resp/ok
      as-json))

(defn get-companies
  [request]
  ;;TODO: should get the company-id as argument ideally
  (-> (db/load-companies)
      resp/ok
      as-json))

(defn github-callback
  [request]
  (as-json
   (resp/ok {:result "Correctly Went throught the whole process"})))

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

(defn rankings-header-rows
  "To make sure that the order is returned correctly we simply sort by
  id both the player names and the rankings returned"
  [players games]
  (let [header (map :name (sort-by :id players))
        csv-rows (for [n (range (inc (count games)))]
                   (map (comp str :ranking)
                        (sort-by :id
                                 (games/get-rankings
                                  (take n games)
                                  players))))]
    [header csv-rows]))

(defn- get-github-token
  [request]
  (get-in request github-token-path))

(defn authenticated?
  [request]
  (let [github-token (get-github-token request)]
    (resp/ok
     {:authenticated (or (not (:auth-enabled config))
                         (some? github-token))
      :token github-token})))

;;TODO: add a not found page for everything else?
(def routes
  ["/" {"api/" {"add-player" add-player!
                "add-game" add-game!

                "league" get-league
                "leagues" get-leagues
                "companies" get-companies
                "players" get-players
                "games" get-games}

        "oauth2/github/callback" github-callback
        "authenticated" authenticated?

        ;; quite a crude way to make sure all the other urls actually
        ;; render to the SPA, letting the routing be handled by
        ;; accountant
        ;; TODO: this might be a problem for things like the ring oauth
        true spa}])

(def routes-handler
  (make-handler routes))

(defn check-token
  [handler]
  ;; return 401 if the request is not authenticated properly
  (fn [request]
    (if (or (not (str/starts-with? (:uri request) "/api"))
            (some? (get-github-token request))
            (not (:auth-enabled config)))

      (handler request)
      (resp/unauthorized "Can not access the given request"))))

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
  (-> routes-handler
      (resources/wrap-resource "public")
      (r-def/wrap-defaults
       (enable-cookies r-def/api-defaults))

      (wrap-authorization basic-auth-backend)
      (wrap-authentication basic-auth-backend)
      wrap-keyword-params
      wrap-json-params
      wrap-json-response
      check-token
      log-request
      (wrap-oauth2 oauth2-config)))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
