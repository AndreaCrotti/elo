(ns elo.dev
  (:require [integrant.core :as ig]
            [clojure.java.io :as io]
            [garden.compiler :refer [compile-css]]
            [ring.adapter.jetty :as jetty]
            [elo.api :as api]
            [figwheel.main.api :as figwheel]
            [ring.middleware.reload :refer [wrap-reload]]
            [integrant.repl :as ir])

  (:gen-class))

(defonce system (atom {}))

(defmethod ig/init-key :server/garden
  [_ {:keys [builds]}]
  (doseq [{:keys [compiler stylesheet id]} builds]
    (.mkdirs (.getParentFile (io/file (:output-to compiler))))
    (require (symbol (namespace stylesheet)) :reload)
    (compile-css compiler (var-get (resolve stylesheet))))

  (mapv #(get-in % [:compiler :output-to]) builds))

(defmethod ig/init-key :server/figwheel
  [_ {:keys [build] :as opts}]
  (figwheel/start build)
  (assert false))

(defmethod ig/init-key :server/jetty
  [_ {:keys [port]}]
  (assert false)
  (jetty/run-jetty (wrap-reload #'api/app)
                   {:join? false
                    :port port}))

(defmethod ig/halt-key! :server/jetty
  [_ server]
  (.stop server))

(def config
  {;;:server/garden {:builds ["screen"]}
   :server/figwheel {:build "elo"}
   :server/jetty {:port 3335}
   })

(defn -main
  [& args]
  (reset! system (ig/init config)))
