(ns byf.notifications
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [byf.config :refer [value]]))

(def slack-hook-url (value :slack-hook))

(defn notify-slack
  [msg]
  (when (some? slack-hook-url)
    (http/post slack-hook-url
               {:content-type :json
                :body (json/write-str {"text" msg})})))
