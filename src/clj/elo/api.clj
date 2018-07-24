(ns elo.api
  (:gen-class)
  (:require [ring.util.response :as resp]
            [ring.middleware.defaults :as r-def]
            [environ.core :refer [env]]
            [compojure.core :refer [defroutes GET POST]]
            [elo.db :refer [store]]
            [ring.adapter.jetty :as jetty]))

(def ^:private default-port 3000)

(defn- get-port
  []
  (Integer. (or (env :port) default-port)))

(defroutes app-routes
  (POST "/store" request ())
  #_(GET "/" request (enter-page request)))

(def app
  (-> app-routes
      (r-def/wrap-defaults {})))

(defn -main [& args]
  (jetty/run-jetty app {:port (get-port)}))
