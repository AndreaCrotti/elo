(ns byf.common.handlers
  (:require [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]))

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

(defn get-league-id
  [db]
  (get-in db [:route-params :league-id]))

(defn loader-no-league-id
  [page uri on-success]
  (fn [_]
    {:http-xhrio {:method :get
                  :uri uri
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn loader
  [page uri on-success]
  (fn [{:keys [db]} _]
    {:http-xhrio {:method :get
                  :uri uri
                  :params {:league_id (get-league-id db)}
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
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

                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
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
