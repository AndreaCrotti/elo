(ns elo.notifications
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def slack-hook-url (:slack-hook env))

(defn notify-slack
  [msg]
  (when (some? slack-hook-url)
    (http/post slack-hook-url
               {:content-type :json
                :body (json/write-str {"text" msg})})))
