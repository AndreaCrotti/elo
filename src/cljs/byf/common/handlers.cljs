(ns byf.common.handlers
  (:require [day8.re-frame.http-fx]
            [ajax.core :as aj]
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

(def request-format (aj/json-request-format))
(def response-format (aj/json-response-format {:keywords? true}))

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
  [db [_ response]]
  (assoc db :error (select-keys response
                                [:status-text :uri :last-method])))

(rf/reg-event-db :failed generic-failed)

(rf/reg-sub :failed (fn [db _]
                      (:error db)))

(defn get-league-id
  [db]
  (get-in db [:route-params :league-id]))

(defn loader-no-league-id
  [page uri on-success]
  (fn [_]
    {:http-xhrio {:method :get
                  :uri uri
                  :format request-format
                  :response-format response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn loader
  [page uri on-success]
  (fn [{:keys [db]} _]
    {:http-xhrio {:method :get
                  :uri uri
                  :params {:league_id (get-league-id db)}
                  :format request-format
                  :response-format response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn writer
  [uri on-success transform-params-fn]
  (fn [{:keys [db]} _]
    {:db db
     :http-xhrio {:method :post
                  :uri uri
                  :params (merge (transform-params-fn db)
                                 {:league_id (get-league-id db)})

                  :format request-format
                  :response-format response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

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
