;;TODO: migrate to always use namespaced keywords
(ns elo.league-detail.handlers
  (:require [cljsjs.moment]
            [elo.common.handlers :as common]
            [elo.games :as games]
            [elo.shared-config :as shared]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))

(rf/reg-sub ::league-id
            (fn [db _]
              (get-in db [:route-params :league-id])))

;;TODO: add some spec validation here
(def default-game
  {:p1 ""
   :p2 ""
   :p1_points ""
   :p2_points ""
   :p1_using ""
   :p2_using ""
   :played_at (js/moment)})

(def default-db
  {:games []
   :players []
   :game {}
   :error nil
   :up-to-games nil
   :league {}
   :league_id nil})

(defn- compute-rankings-data
  [query-v _]
  [(rf/subscribe [::games])
   (rf/subscribe [::players])
   (rf/subscribe [::up-to-games])])

(defn- truncate-games
  [games up-to-games]
  (if (some? up-to-games)
    (take up-to-games games)
    games))

(rf/reg-sub ::rankings
            compute-rankings-data
            (fn [[games players up-to-games] _]
              (let [rankings
                    (games/get-rankings (truncate-games games up-to-games) players)]
                (sort-by #(- (second %)) rankings))))

(defn games-signal
  [query-v _]
  [(rf/subscribe [::games]) (rf/subscribe [::up-to-games])])

(rf/reg-sub ::results
            games-signal
            (fn [[gs up-to] _]
              (games/results (truncate-games gs up-to))))

(rf/reg-sub ::stats
            games-signal
            (fn [[gs up-to] _]
              (games/summarise (truncate-games gs up-to))))

(defn prev-game
  [db _]
  (let [up-to @(rf/subscribe [::up-to-games])
        games @(rf/subscribe [::games])]

    (if (nil? up-to)
      (common/assoc-in* db page [:up-to-games] (dec (count games)))
      (if (pos? up-to)
        (common/update-in* db page [:up-to-games] dec)
        db))))

(defn next-game
  [db _]
  (let [up-to @(rf/subscribe [::up-to-games])
        games @(rf/subscribe [::games])]

    (if (< up-to (count games))
      (common/update-in* db page [:up-to-games] inc)
      db)))

(rf/reg-event-db ::prev-game prev-game)
(rf/reg-event-db ::next-game next-game)

(rf/reg-sub ::name-mapping
            (fn [query-v _]
              [(rf/subscribe [::players])])

            (fn [[players] _]
              (games/player->names players)))

(rf/reg-sub ::rankings-data
            compute-rankings-data
            ;;TODO: might be nice also to have a from-games to slice even more nicely
            (fn [[games players up-to-games] _]
              (let [x-axis (range up-to-games)
                    compute-games (fn [up-to] (games/get-rankings (if (some? up-to)
                                                                    (take up-to games)
                                                                    games)
                                                                  players))
                    all-rankings (map compute-games x-axis)
                    grouped (group-by :id (flatten all-rankings))]

                (into {}
                      (for [[k v] grouped]
                        {k (map :ranking v)})))))

(rf/reg-sub ::error (getter [:error]))

(rf/reg-sub ::game-type
            (fn [db _]
              (common/get-in* db page [:league :game_type])))

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

(rf/reg-sub ::valid-players?
            (fn [query-v _]
              [(rf/subscribe [::game])])

            (fn [[game] _]
              (valid-players? game)))

(rf/reg-sub ::valid-result?
            (fn [query-v _]
              [(rf/subscribe [::game-type])
               (rf/subscribe [::game])])

            (fn [[game-type game] _]
              (valid-result? game-type game)))

(rf/reg-sub ::filled-game?
            (fn [db _]
              (not-any? #(= % "")
                        (vals (common/get-in* db page [:game])))))

(rf/reg-sub ::valid-game?
            (fn [query-v _]
              [(rf/subscribe [::valid-result?])
               (rf/subscribe [::filled-game?])
               (rf/subscribe [::valid-players?])])

            (fn [[valid-result? filled-game? valid-players?] _]
              (and valid-result?
                   filled-game?
                   valid-players?)))

(rf/reg-event-db ::reset-game (fn [db _]
                                (common/assoc-in* db page [:game] default-game)))

(rf/reg-sub ::game (getter [:game]))
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
(rf/reg-sub ::players (getter [:players]))

(rf/reg-event-db ::initialize-db
                 (fn [db _]
                   (assoc db
                          page
                          (assoc default-db :game default-game))))

(rf/reg-event-db ::p1 (setter [:game :p1]))
(rf/reg-event-db ::p1_points (setter [:game :p1_points]))
(rf/reg-event-db ::p1_using (setter [:game :p1_using]))
(rf/reg-event-db ::up-to-games (setter [:up-to-games]))

(rf/reg-event-db ::p2 (setter [:game :p2]))
(rf/reg-event-db ::p2_points (setter [:game :p2_points]))
(rf/reg-event-db ::p2_using (setter [:game :p2_using]))

(rf/reg-event-db ::played_at (setter [:game :played_at]))

(defn- reload-fn-gen
  [extra-signal]
  (fn [{:keys [db]} _]
    (js/alert "Thanks you, results and rankings are updated immediately")
    ;;TODO: would be nice to trigger a transaction of the interested
    ;;area of the page to make it clear what was actually changed
    {:db db
     :dispatch-n (cons extra-signal [[::load-players]
                                     [::load-games]])}))

(rf/reg-event-fx ::add-game-success (reload-fn-gen [::reset-game]))

(rf/reg-event-db ::failed (common/failed page))

(rf/reg-event-db ::load-games-success (setter [:games]))
(rf/reg-event-db ::load-players-success (setter [:players]))
(rf/reg-event-db ::load-league-success (setter [:league]))

(rf/reg-event-fx ::load-games (common/loader page "/api/games" ::load-games-success))
(rf/reg-event-fx ::load-players (common/loader page "/api/players" ::load-players-success))
(rf/reg-event-fx ::load-league (common/loader page "/api/league" ::load-league-success))

(defn game-transform
  [db]
  (update
   (common/get-in* db page [:game])
   :played_at
   #(.format % shared/timestamp-format)))

(rf/reg-event-fx ::add-game (common/writer page "/api/add-game"
                                           ::add-game-success game-transform))
