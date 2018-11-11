(ns elo.dev
  (:require [clojure.java.io :as io]
            [elo.api :as api]
            [figwheel.main.api :as figwheel]
            [garden.compiler :refer [compile-css]]
            [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [taoensso.timbre :as log])

  (:gen-class))

(defonce system (atom {}))

(def garden-options
  [{:id "screen"
    :source-paths ["src/clj" "src/cljc"]
    :stylesheet "elo.css"
    :compiler {:output-to "resources/public/css/screen.css"
               :pretty-print? true}}])

(defn start-garden
  [builds]
  (doseq [{:keys [compiler stylesheet id]} builds]
    (log/info (str "Loading build " id))
    (.mkdirs (.getParentFile (io/file (:output-to compiler))))
    (require (symbol stylesheet) :reload)
    (log/infof "writing to file %s" (:output-to compiler))
    (compile-css compile-css
                 (var-get (get (ns-map (symbol stylesheet))
                               (symbol id)))))
  (mapv #(get-in % [:compiler :output-to]) builds))

(defmethod ig/init-key :server/garden
  [_ _]
  (log/info "Starting Garden")
  (start-garden garden-options))

(defmethod ig/halt-key! :server/garden
  [_ output-files]
  (log/info "stopping Garden"))

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
  {:server/garden {}
   :server/figwheel {:build "elo"}
   :server/jetty {:port 3335}
   :server/nrepl {}})

(defn -main
  [& args]
  (reset! system (ig/init config)))
