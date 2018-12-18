(ns elo.common.sets
  (:require [elo.common.handlers :as common]))

(defn clear
  [page key]
  (fn [db _]
    (common/assoc-in* db page [key] #{})))

(defn fill
  [page key reset-fn]
  (fn [db _]
    (common/assoc-in* db page [key] (set (reset-fn db)))))

(defn modify
  [page key action]
  (fn [db [_ uuid]]
    (let [func (case action
                 :disj disj
                 :conj conj)]

      (common/update-in* db
                         page
                         [key]
                         #(func % uuid)))))

(defn in?
  [page key]
  (fn [db [_ uuid]]
    (contains? (common/get-in* db page [key]) uuid)))
