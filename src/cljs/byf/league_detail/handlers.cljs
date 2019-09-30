;;TODO: migrate to always use namespaced keywords
(ns byf.league-detail.handlers
  (:require [cljsjs.moment]
            [clojure.set :as set]
            [byf.common.handlers :as common]
            [byf.common.players :as players-handlers]
            [byf.common.sets :as sets]
            [byf.games :as games]
            [byf.rankings :as rankings]
            [byf.stats :as stats]
            [byf.shared-config :as shared]
            [reagent.cookies :as ck]
            [medley.core :as medley]
            [re-frame.core :as rf]))

(def cookie-validity (* 365 24 60 60))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))

(rf/reg-sub ::league-id
            (fn [db _]
              (get-in db [:route-params :league-id])))

(rf/reg-sub ::league-name
            (fn [db _]
              (get-in db [::page-id :league :name])))

;; should I just add a watcher to the reagent db to get the desired checks?

(def default-db
  {:games []
   :dead-players #{}
   :hidden-players #{}
   :game {}
   :error nil
   :up-to-games nil
   :league {}
   :league_id nil
   :show-all? false
   :game-config shared/default-game-config
   :add-user-notification false
   :current-user-notification false
   :loading? true
   :show-graph false
   :show-results false
   :current-user nil})

(defn- truncate-games
  [games up-to-games]
  (if (some? up-to-games)
    (take up-to-games games)
    games))

(rf/reg-sub ::current-user (getter [:current-user]))
(rf/reg-event-fx ::set-current-user
                 (fn [{:keys [db]} [_ value]]
                   {:db (common/assoc-in* db page [:current-user] value)
                    :dispatch [::p1 value]}))

;; can use the re-frame library to handle this more nicely
(rf/reg-event-fx ::store-current-user
                 (fn [{:keys [db]}]
                   (let [current-user (common/get-in* db page [:current-user])]
                     (ck/set! :user current-user :max-age cookie-validity)
                     {:dispatch [::current-user-notification true]})))

(rf/reg-sub ::game-config (getter [:game-config]))
(rf/reg-event-db ::k (setter [:game-config :k]))
(rf/reg-event-db ::initial-ranking (setter [:game-config :initial-ranking]))

(defn uuid->name
  [name-mapping vals]
  (medley/map-keys #(get name-mapping %) vals))

(rf/reg-sub ::add-user-notification (getter [:add-user-notification]))
(rf/reg-event-db ::add-user-notification
                 (fn [db _]
                   (common/assoc-in* db page [:add-user-notification] true)))

(rf/reg-event-db ::clear-notification
                 (fn [db _]
                   (common/assoc-in* db page [:add-user-notification] false)))

(rf/reg-sub ::current-user-notification (getter [:current-user-notification]))
(rf/reg-event-db ::current-user-notification
                 (fn [db _]
                   (common/assoc-in* db page [:current-user-notification] true)))

(rf/reg-event-db ::clear-set-user-notification
                 (fn [db _]
                   (common/assoc-in* db page [:current-user-notification] false)))

(rf/reg-sub ::show-graph (getter [:show-graph]))
(rf/reg-sub ::show-results (getter [:show-results]))
(rf/reg-sub ::loading? (getter [:loading?]))

(rf/reg-event-db ::toggle-graph
                 (fn [db _]
                   (common/update-in* db page [:show-graph] not)))

(rf/reg-event-db ::toggle-results
                 (fn [db _]
                   (common/update-in* db page [:show-results] not)))

(rf/reg-sub ::rankings
            :<- [::games-live-players]
            :<- [::players-handlers/players]
            :<- [::up-to-games]
            :<- [::dead-players]
            :<- [::game-config]

            (fn [[games players up-to-games dead-players game-config] _]
              (rankings/rankings games players up-to-games dead-players game-config)))

(rf/reg-sub ::results
            :<- [::games-live-players]
            :<- [::up-to-games]

            (fn [[gs up-to] _]
              (games/results (truncate-games gs up-to))))

(rf/reg-sub ::stats
            :<- [::games-live-players]
            :<- [::up-to-games]

            (fn [[gs up-to] _]
              (games/summarise (truncate-games gs up-to))))

(rf/reg-sub ::rankings-history
            :<- [::players-handlers/players]
            :<- [::visible-players]
            :<- [::games-live-players]
            :<- [::up-to-games]

            (fn [[players visible-players games up-to]]
              (rankings/rankings-history players visible-players games up-to)))

(rf/reg-sub ::last-game-played-by
            :<- [::games-live-players]
            :<- [::up-to-games]
            :<- [::players-handlers/name-mapping]

            (fn [[games up-to-games name-mapping]]
              (let [up-to (or up-to-games
                              (count games))]

                (when (pos? up-to-games)
                  (->> ((juxt :p1 :p2)
                        (nth games (dec up-to)))
                       (map name-mapping)
                       set)))))

(rf/reg-sub ::last-ranking-changes-by-player
            :<- [::rankings-history]
            :<- [::last-game-played-by]

            (fn [[rankings-history last-games-played-by]]
              (rankings/last-ranking-changes rankings-history last-games-played-by)))

(rf/reg-sub ::rankings-domain
            :<- [::games-live-players]
            :<- [::players-handlers/players]

            (fn [[games players]]
              (rankings/domain games players)))

(defn prev-game
  [db _]
  (let [up-to @(rf/subscribe [::up-to-games])
        games @(rf/subscribe [::games-live-players])]

    (if (nil? up-to)
      (common/assoc-in* db page [:up-to-games] (dec (count games)))
      (if (pos? up-to)
        (common/update-in* db page [:up-to-games] dec)
        db))))

(defn next-game
  [db _]
  (let [up-to @(rf/subscribe [::up-to-games])
        games @(rf/subscribe [::games-live-players])]

    (if (< up-to (count games))
      (common/update-in* db page [:up-to-games] inc)
      db)))

(rf/reg-event-db ::prev-game prev-game)
(rf/reg-event-db ::next-game next-game)

(rf/reg-sub ::error (getter [:error]))

(rf/reg-sub ::game-type
            (fn [db _]
              (common/get-in* db page [:league :game_type])))

(rf/reg-sub ::game (getter [:game]))
;;TODO: add here the default condition
(rf/reg-sub ::up-to-games
            (fn [db _]
              (some-> (common/get-in* db page [:up-to-games])
                      js/parseInt)))

(rf/reg-sub ::league
            (fn [db _]
              (update
               (common/get-in* db page [:league])
               :game_type
               keyword)))

(rf/reg-sub ::games (getter [:games]))

(rf/reg-sub ::games-live-players
            :<- [::games]
            :<- [::dead-players]

            (fn [[games dead-players]]
              (let [inner (fn [field v] (not (contains? dead-players (field v))))]
                (filter #(and (inner :p1 %) (inner :p2 %)) games))))

(defn current-user-transform
  [db]
  (let [current-user (ck/get :user)]
    (if (some? current-user)
      ;;TODO: need to set the current user as well
      ;;and maybe have a cascade effect there
      (-> db
          (assoc-in [:game :p1] current-user)
          (assoc-in [:current-user] current-user))
      db)))

(rf/reg-event-db ::initialize-db
                 (fn [db _]
                   (assoc db page default-db)))

(rf/reg-event-db ::load-games-success
                 (fn [db [_ games]]
                   (-> db
                       (common/assoc-in* page [:games] games)
                       (common/assoc-in* page [:loading?] false))))

(rf/reg-event-db ::load-league-success (setter [:league]))

(rf/reg-event-fx ::load-games (common/loader page "/api/games" ::load-games-success))
(rf/reg-event-fx ::load-league (common/loader page "/api/league" ::load-league-success))

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

(rf/reg-sub ::hidden? (sets/in? page :hidden-players))

(rf/reg-sub ::hidden-players (getter [:hidden-players]))

(rf/reg-event-db ::show (sets/modify page :hidden-players :disj))

(rf/reg-event-db ::hide (sets/modify page :hidden-players :conj))

(defn fill-all
  [kw]
  (sets/fill page kw
             #(map :id (common/get-in* % players-handlers/page [:players]))))

(rf/reg-event-db ::hide-all (fill-all :hidden-players))

(rf/reg-event-db ::show-all (sets/clear page :hidden-players))

;; dead players
(rf/reg-sub ::dead? (sets/in? page :dead-players))

(rf/reg-sub ::dead-players (getter [:dead-players]))

(rf/reg-event-db ::revive (sets/modify page :dead-players :disj))

(rf/reg-event-db ::kill (sets/modify page :dead-players :conj))

(rf/reg-event-db ::kill-all (fill-all :dead-players))

(rf/reg-event-db ::revive-all (sets/clear page :dead-players))

(rf/reg-sub ::visible-players
            :<- [::players-handlers/players]
            :<- [::hidden-players]
            :<- [::players-handlers/active-players]

            (fn [[players hidden-players active-players]]
              (filter #(and (not (hidden-players (:id %)))
                            (active-players (:id %)))
                      players)))

(rf/reg-sub ::rankings-history-vega
            :<- [::rankings-history]

            (fn [history]
              (let [kw->keyname {:player "Player"
                                 :ranking "Ranking"
                                 :game-idx "Game #"
                                 :time "Time"}]

                (->> history
                     (map #(update % :game-idx inc))
                     (map #(set/rename-keys % kw->keyname))))))

(rf/reg-sub ::highest-rankings-best
            :<- [::rankings-history]

            (fn [history]
              (stats/highest-rankings-best history)))

(rf/reg-sub ::longest-winning-streaks
            :<- [::results]
            :<- [::players-handlers/name-mapping]

            (fn [[results name-mapping]]
              (stats/longest-streak results name-mapping)))

(rf/reg-sub ::longest-unbeaten-streaks
            :<- [::results]
            :<- [::players-handlers/name-mapping]

            (fn [[results name-mapping]]
              (stats/longest-unbeaten results name-mapping)))

(rf/reg-sub ::highest-increase
            :<- [::rankings-history]

            (fn [history]
              (stats/highest-increase history)))

(rf/reg-sub ::best-percents
            :<- [::results]
            :<- [::players-handlers/name-mapping]

            (fn [[results name-mapping]]
              (stats/best-percents results name-mapping)))

(rf/reg-event-fx ::toggle-show-all
                 (fn [{:keys [db]} _]
                   {:dispatch [::load-games]
                    :db (update-in db
                                   [page :show-all?]
                                   not)}))

(rf/reg-sub ::show-all? (getter [:show-all?]))

(rf/reg-sub ::from-game (getter [::from-game]))
(rf/reg-sub ::to-game (getter [::to-game]))

(rf/reg-event-db ::from-game (setter [::from-game]))
(rf/reg-event-db ::to-game (setter [::to-game]))
