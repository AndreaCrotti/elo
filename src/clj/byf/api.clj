(ns byf.api
  (:gen-class)
  (:require [bidi.ring :refer [make-handler]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [byf.auth :refer [basic-auth-backend with-basic-auth]]
            [byf.config :refer [value]]
            [byf.db :as db]
            [byf.notifications :as notifications]
            [byf.pages.home :as home]
            [byf.validate :as validate]
            [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [hiccup.core :as hiccup]
            [medley.core :as medley]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as r-def]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.resource :as resources]
            [ring.util.http-response :as resp]
            [ring.util.response]
            [taoensso.timbre :as timbre :refer [info]])
  (:import (java.util UUID)))

(def max-age (* 60 60 24 10))

(defn transaction-middleware
  [handler]
  (fn [request]
    (db/wrap-db-call
     (jdbc/with-db-transaction [tx (db/db-spec)]
       (handler request)))))

(defn uuid-to-str
  [m]
  (medley/map-vals str m))

(defn my-json-writer
  [k v]
  (cond
    (inst? v) (str v)
    (uuid? v) (str v)
    :else v))

(defn encode
  [response]
  (-> response
      (update :body #(json/write-str % :value-fn my-json-writer :key-fn name))
      ;; add the actual magic update
      (resp/content-type "application/json")))

(defn format-game
  [params]
  (let [get-name #(:name (db/get-single db/player-name (UUID/fromString %)))]
    (format "%s (%s), %s - %s, (%s) %s"
            ;; convert :p1 and :p2 to the username
            (get-name (:p1 params))
            (:p1_using params)
            (:p1_points params)
            (:p2_points params)
            (:p2_using params)
            (get-name (:p2 params)))))

(defn add-game!
  [{:keys [params]}]
  (notifications/notify-slack (format-game params))
  (let [validated (validate/conform-data :game params)
        game-id (db/add-game! validated)]

    (encode
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

      (encode
       (resp/created "/api/players" ids)))))

(defn- render-page
  [page]
  (resp/content-type
   (resp/ok
    (hiccup/html page))

   "text/html"))

(defn spa
  [request]
  (render-page (home/body request)))

(defn- get-league-id
  [request]
  (-> request
      :params
      :league_id
      validate/to-uuid))

(defn- get-player-id
  [request]
  (-> request
      :params
      :player_id
      validate/to-uuid))

(defn get-players
  [request]
  (-> (get-league-id request)
      db/load-players
      resp/ok
      encode))

(defn get-games*
  [league-id]
  (map
   #(select-keys % [:p1 :p1_points :p1_using
                    :p2 :p2_points :p2_using
                    :played_at])
   (db/load-games league-id)))

(defn get-games
  [request]
  (-> (get-league-id request)
      get-games*
      resp/ok
      encode))

(defn get-league
  [request]
  (-> (get-league-id request)
      db/load-league
      resp/ok
      encode))

(defn get-leagues
  [request]
  ;;TODO: should get the company-id as argument ideally
  (-> (db/load-leagues)
      resp/ok
      encode))

(defn get-companies
  [request]
  ;;TODO: should get the company-id as argument ideally
  (-> (db/load-companies)
      resp/ok
      encode))

(defn toggle-player!
  [request]
  (let [league-id (get-league-id request)
        player-id (get-player-id request)
        active_str (-> request :params :active)
        active (if (= "true" active_str) true false)]
    (db/toggle-player! league-id player-id active)
    (resp/created
     "api/players"
     {:player-id player-id
      :active    active
      :league_id league-id})))

;;TODO: add a not found page for everything else?
(def routes
  ["/" {"api/" {"add-player" add-player!
                "add-game" add-game!
                "toggle-player" toggle-player!

                "league" get-league
                "leagues" get-leagues
                "companies" get-companies
                "players" get-players
                "games" get-games}

        ;; quite a crude way to make sure all the other urls actually
        ;; render to the SPA, letting the routing be handled by
        ;; accountant
        ;; TODO: this might be a problem for things like the ring oauth
        true spa}])

(def routes-handler
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

(defn add-cache-control
  [handler]
  (fn [request]
    (ring.util.response/header
     (handler request)
     "Cache-Control" (format "max-age=%s" max-age))))

(def app
  (-> routes-handler
      (resources/wrap-resource "public")
      #_wrap-not-modified
      wrap-gzip
      (r-def/wrap-defaults
       (enable-cookies r-def/api-defaults))

      (wrap-authorization basic-auth-backend)
      (wrap-authentication basic-auth-backend)
      #_add-cache-control
      wrap-keyword-params
      wrap-json-params
      wrap-json-response
      #_check-token
      log-request
      transaction-middleware))

(defn -main [& args]
  (jetty/run-jetty app {:host "0.0.0.0"
                        :port (-> :port
                                  value
                                  Integer/parseInt)}))
