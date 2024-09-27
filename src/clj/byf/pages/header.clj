(ns byf.pages.header
  (:require [clojure.data.json :as json]
            [byf.config :refer [load-config]]
            [byf.pages.utils :refer [cache-buster]]))

(defn- google-font
  [font-name]
  [:link {:rel "stylesheet"
          :href (format "//fonts.googleapis.com/css?family=%s" font-name)}])

(def fonts
  {:titles         "Monoton"
   :smaller-titles "Lilita+One"})

(def shared-keys
  [:auth-enabled
   :firebase-auth-domain
   :firebase-api-key])

(defn global-client-side-config
  []
  (-> (load-config)
      (select-keys shared-keys)
      (json/write-str)))

(defn css
  [path]
  [:link {:href (cache-buster (str "/css/" path))
          :rel  "stylesheet"
          :type "text/css"}])

(defn gen-header
  [title]
  [:head [:meta {:charset "utf-8"
                 :description "Internal leagues platform"}]

   [:script (format "window['cfg']=%s" (global-client-side-config))]

   [:script {:src "/js/newrelic.js"}]

   [:title title]

   (google-font (:titles fonts))
   (google-font (:smaller-titles fonts))

   [:link {:rel "stylesheet"
           :href "https://cdnjs.cloudflare.com/ajax/libs/antd/3.9.3/antd.min.css"}]
   ;; should we get different packages?

   [:script {:src "https://cdn.jsdelivr.net/npm/vega@4.2.0"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@3.0.0-rc6"}]
   [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@3.19.2"}]

   (css "vega_embed.css")
   (css "tweaks.css")
   (css "spinner.css")
   (css "slider.css")])
