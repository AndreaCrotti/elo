(ns elo.common.handlers)

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
  [page-id ks]
  (fn [db [_ val]]
    (assoc-in* db page-id ks val)))
