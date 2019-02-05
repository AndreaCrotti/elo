(ns elo.dev
  (:require [elo.api :as api]
            [figwheel.main.api :as figwheel]
            [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [taoensso.timbre :as log])

  (:gen-class))

(defonce system (atom {}))

(defmethod ig/init-key :server/figwheel
  [_ {:keys [build] :as opts}]
  (log/info "Starting Figwheel")
  (figwheel/start build))

(defmethod ig/init-key :server/jetty
  [_ {:keys [port]}]
  (log/info "Starting Jetty")
  (jetty/run-jetty (wrap-reload #'api/app)
                   {:join? false
                    :port port}))

(defmethod ig/halt-key! :server/jetty
  [_ server]
  (.stop server))

(defmethod ig/init-key :server/nrepl
  [_ {:keys [port]}]
  (log/info "Starting Nrepl"))

(def config
  {:server/figwheel {:build "elo"}
   :server/jetty {:port 3335}
   :server/nrepl {}})

(defn -main
  [& args]
  (reset! system (ig/init config)))
