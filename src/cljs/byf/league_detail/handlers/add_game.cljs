(ns byf.league-detail.handlers.add-game
  (:require [re-frame.core :as rf]
            [byf.common.players :as players-handlers]
            [byf.common.handlers :as common]
            [byf.shared-config :as shared]))

(def default-game
  {:p1 ""
   :p2 ""
   :p1_points ""
   :p2_points ""
   :p1_using ""
   :p2_using ""
   :played_at (js/moment)})

(def page ::page-id)

(rf/reg-event-db ::reset-game
                 (fn [db _]
                   (common/assoc-in* db page [:game] default-game)))

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))

(rf/reg-event-db ::p1 (setter [:game :p1]))
(rf/reg-event-db ::p1_points (setter [:game :p1_points]))
(rf/reg-event-db ::p1_using (setter [:game :p1_using]))

(rf/reg-event-db ::p2 (setter [:game :p2]))
(rf/reg-event-db ::p2_points (setter [:game :p2_points]))
(rf/reg-event-db ::p2_using (setter [:game :p2_using]))
(rf/reg-event-db ::played_at (setter [:game :played_at]))

(rf/reg-sub ::game (getter [:game]))

(defn valid-players?
  [{:keys [p1 p2]}]
  (not= p1 p2))

;; I could return some kind of coeffect when there is an error
;; to give a better warning in the UI
(defn valid-result?
  [game-type game]
  (let [p1 (:p1_points game)
        p2 (:p2_points game)
        draw? (-> shared/games-config :fifa :draw?)]

    (or draw?
        (not= p1 p2))))


(rf/reg-sub ::players
            (constantly ["one" "two" "three"]))

(rf/reg-sub ::valid-players?
            :<- [::game]

            (fn [game _]
              (valid-players? game)))

(rf/reg-sub ::valid-result?
            :<- [::game]
            :<- [::game-type]

            (fn [[game-type game] _]
              (valid-result? game-type game)))

(rf/reg-sub ::filled-game?
            (fn [db _]
              (not-any? #(= % "")
                        (vals (common/get-in* db page [:game])))))


(rf/reg-sub ::valid-game?
            :<- [::valid-result?]
            :<- [::filled-game?]
            :<- [::valid-players?]

            (fn [[valid-result? filled-game? valid-players?] _]
              (and valid-result?
                   filled-game?
                   valid-players?)))

(defn- reload-fn-gen
  [extra-signal]
  (fn [{:keys [db]} _]
    {:db db
     :dispatch-n (cons extra-signal [[::add-user-notification]
                                     [::players-handlers/load-players]
                                     [::load-games]])}))

(rf/reg-event-fx ::add-game-success (reload-fn-gen [::reset-game]))

(defn game-params
  [db]
  (update
   (common/get-in* db page [:game])
   :played_at
   #(.format % shared/timestamp-format)))

(rf/reg-event-fx ::add-game
                 (common/writer "/api/add-game"
                                ::add-game-success
                                game-params))
