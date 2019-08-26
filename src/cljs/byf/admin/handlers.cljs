(ns byf.admin.handlers
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [byf.common.handlers :as common]
            [clojure.spec.alpha :as s]))

(def page ::page-id)

(def getter (partial common/getter* page))

(def setter (partial common/setter* page))

(def default-player
  {:name ""
   :email ""
   :league_id nil})

(def default-db
  {:player default-player
   :leagues []})

(s/def ::name string?)
(s/def ::email string?)
;; actually a UUID check would be better
(s/def ::league_id (s/nilable string?))

;; a better spec for this??
(s/def ::leagues sequential?)
(s/def ::player (s/keys :req-un [::name ::email ::league_id]))
(s/def ::db (s/keys :req-un [::player ::leagues]))

(def safe-event-db (common/->safe-event-db page ::db))

(safe-event-db ::name
               (setter [:player :name]))

(safe-event-db ::email
               (setter [:player :email]))

(safe-event-db ::league
               (setter [:player :league_id]))

(rf/reg-sub ::league (getter [:league]))
(rf/reg-sub ::leagues (getter [:leagues]))
(rf/reg-sub ::player (getter [:player]))

(rf/reg-sub ::valid-player?
            (fn [db _]
              (not-any? #(= % "")
                        (vals (common/get-in* db page [:player])))))

(rf/reg-event-fx ::add-player-success
                 (fn [{:keys [db]} _]
                   (js/alert "Thanks")))

(safe-event-db ::reset-player
               (fn [db _]
                 (common/assoc-in* db page [:player] default-player)))

(defn writer
  [page uri on-success]
  (fn [{:keys [db]} _]
    {:http-xhrio {:method :post
                  :uri uri
                  :params (common/get-in* db page [:player])
                  :format common/request-format
                  :response-format common/response-format
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(rf/reg-event-fx ::add-player (writer page
                                      "/api/add-player"
                                      ::add-player-success))

(safe-event-db ::load-leagues-success
               (setter [:leagues]))

(rf/reg-event-fx ::load-leagues
                 (common/loader-no-league-id
                  page
                  "/api/leagues"
                  ::load-leagues-success))

(safe-event-db ::initialize-db
               (fn [db _]
                 (assoc db page default-db)))
