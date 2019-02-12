(ns byf.system
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [taoensso.timbre :as log]))

(defmethod ig/halt-key! :server/jetty
  [_ server]
  (.stop server))

(defmethod ig/init-key :server/jetty
  [_ {:keys [port handler]}]
  (log/info "Starting Jetty")
  (jetty/run-jetty (wrap-reload handler)
                   {:join? false
                    :port (Integer/parseInt port)}))
