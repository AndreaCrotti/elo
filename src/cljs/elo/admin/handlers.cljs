(ns elo.admin.handlers
  (:require [re-frame.core :as rf]
            [elo.common.handlers :as common]))

(def page ::page-id)

(def getter (partial common/getter* page))

(def setter (partial common/getter* page))

(def default-player
  {:name ""
   :email ""})

(rf/reg-event-db :name (setter [:player :name]))
(rf/reg-event-db :email (setter [:player :email]))


(rf/reg-sub :player (getter [:player]))

(rf/reg-sub :valid-player?
            (fn [db _]
              (not-any? #(= % "")
                        (vals (common/get-in* db page [:player])))))

(rf/reg-event-fx :add-player-success (fn [{:keys [db]} _]
                                       (js/alert "Thanks")))

(rf/reg-event-db :reset-player (fn [db _]
                                 (common/assoc-in* db page [:player] default-player)))

(defn player-transform
  [db]
  (common/get-in* db page [:player]))

(rf/reg-event-fx :add-player (common/writer page "/api/add-player"
                                            :add-player-success player-transform))
