(ns user
  (:require [clojure.spec.alpha :as s]
            [expound.alpha :as e]
            [integrant.repl :as ir]
            [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [byf.api :as api]
            [byf.config :as c]
            [ring.middleware.reload :refer [wrap-reload]]
            [taoensso.timbre :as log]))

(def printer
  (e/custom-printer {:show-valid-values? true
                     :print-specs? true
                     :theme :figwheel-theme}))

(defmethod ig/halt-key! :server/jetty
  [_ server]
  (.stop server))

(defmethod ig/init-key :server/jetty
  [_ {:keys [port]}]
  (log/info "Starting Jetty")
  (jetty/run-jetty (wrap-reload #'api/app)
                   {:join? false
                    :port port}))

(alter-var-root #'s/*explain-out* (constantly printer))

(s/check-asserts true)

(ir/set-prep! (constantly {:server/jetty {:port (-> :port c/value Integer/parseInt)}}))
