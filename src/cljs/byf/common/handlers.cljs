(ns byf.common.handlers
  (:require [day8.re-frame.http-fx]
            [cljs.pprint :as pprint]
            [cljs.reader :as reader]
            [ajax.interceptors :as ajax-interceptors]
            [ajax.protocols :as ajax-protocols]
            [re-frame.core :as rf]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]))

(def edn-request-format
  {:write #(with-out-str (pprint/pprint %))
   :content-type "application/edn"})

(defn- edn-read-fn
  [response]
  (reader/read-string (ajax-protocols/-body response)))

(def edn-response-format
  (ajax-interceptors/map->ResponseFormat
   {:read edn-read-fn
    :description "EDN"
    :content-type ["application/edn"]}))

(defn get-in*
  [m page-id ks]
  (get-in m (cons page-id ks)))

(defn assoc-in*
  [m page-id ks v]
  (assoc-in m (cons page-id ks) v))

(defn update-in*
  [m page-id ks update-fn]
  (update-in m (cons page-id ks) update-fn))

(defn getter*
  [page-id ks]
  (fn [db _]
    (get-in* db page-id ks)))

(defn setter*
  ([page-id ks spec]
   (let [new-db (setter* page-id ks)]
     (s/assert (s/conform spec new-db) (s/explain spec new-db))
     new-db))

  ([page-id ks]
   (fn [db [_ val]]
     (assoc-in* db page-id ks val))))

(defn generic-failed
  [db [_ {:keys [status-text]}]]
  (assoc db :error status-text))

(rf/reg-event-db :failed generic-failed)

(rf/reg-sub :failed :error)

(defn get-league-id
  [db]
  (get-in db [:route-params :league-id]))

(defn loader-no-league-id
  [page uri on-success]
  (fn [_]
    {:http-xhrio {:method :get
                  :uri uri
                  :format edn-request-format
                  :response-format edn-response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn loader
  [page uri on-success]
  (fn [{:keys [db]} _]
    {:http-xhrio {:method :get
                  :uri uri
                  :params {:league_id (get-league-id db)}
                  :format edn-request-format
                  :response-format edn-response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn writer
  [page uri on-success transform-params-fn]
  (fn [{:keys [db]} _]
    {:db db
     :http-xhrio {:method :post
                  :uri uri
                  :params (merge (transform-params-fn db)
                                 {:league_id (get-league-id db)})

                  :format edn-request-format
                  :response-format edn-response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn failed
  [page]
  (fn [db [_ {:keys [status parse-error] :as req}]]
    (assoc-in* db page
               [:error]
               {:status status
                :status-text (:status-text parse-error)
                :original-text (:original-text parse-error)})))

(rf/reg-event-db :set-route-params
                 (fn [db [_ route-params]]
                   (assoc db :route-params route-params)))

(defn- ->check-db-interceptor
  [page db-spec]
  (rf/->interceptor
   :id :validate
   :after (fn [context]
            (let [local-db (-> context :effects :db page)]
              (if (s/valid? db-spec local-db)
                context
                (throw (ex-info (str "spec check failed: " (s/explain-str db-spec local-db)) {})))))))

(defn ->safe-event-db
  [page db-spec]
  (fn [id handler]
    (rf/reg-event-db
     id
     [(->check-db-interceptor page db-spec)]
     handler)))

(defn set-random-db
  "Handler capable of simply setting a random intial db.
  This can be used instead of initialise-db for example."
  [db-spec]
  (fn [db _]
    (gen/generate db-spec)))
