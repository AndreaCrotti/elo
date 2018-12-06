;;TODO: migrate to always use namespaced keywords
(ns elo.league-detail.handlers
  (:require [cljsjs.moment]
            [clojure.set :as set]
            [elo.common.handlers :as common]
            [elo.games :as games]
            [elo.shared-config :as shared]
            [medley.core :as medley]
            [re-frame.core :as rf]))

(def page ::page-id)

(def setter (partial common/setter* page))

(def getter (partial common/getter* page))

(rf/reg-sub ::league-id
            (fn [db _]
              (get-in db [:route-params :league-id])))

;; should I just add a watcher to the reagent db to get the desired checks?

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
   :dead-players #{}
   :hidden-players #{}
   :game {}
   :error nil
   :up-to-games nil
   :league {}
   :league_id nil
   :show-all? false})

(defn- truncate-games
  [games up-to-games]
  (if (some? up-to-games)
    (take up-to-games games)
    games))

(rf/reg-sub ::rankings
            :<- [::games-live-players]
            :<- [::players]
            :<- [::up-to-games]
            :<- [::dead-players]

            (fn [[games players up-to-games dead-players] _]
              (let [rankings
                    (games/get-rankings (truncate-games games up-to-games) players)
                    updated (map #(if (contains? dead-players (:id %))
                                    (assoc % :ranking 0) %)
                                 rankings)]

                (sort-by #(- (:ranking %)) updated))))

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

;;TODO: overcomplicated way of computing history making sure we only
;;show game of shown players
(rf/reg-sub ::rankings-history
            :<- [::players]
            :<- [::visible-players]
            :<- [::games-live-players]
            :<- [::up-to-games]

            (fn [[players visible-players games up-to] _]
              (let [visible-players-names (set (map :name visible-players))
                    full-rankings
                    (games/rankings-history players (truncate-games games up-to))]

                (->> full-rankings
                     (filter #(contains? visible-players-names (:player %)))))))

(rf/reg-sub ::rankings-domain
            :<- [::games]
            :<- [::players]

            (fn [[games players]]
              (let [full-rankings-history (games/rankings-history players games)]
                [(apply min (map :ranking full-rankings-history))
                 (apply max (map :ranking full-rankings-history))])))

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

(rf/reg-sub ::name-mapping
            :<- [::players]

            (fn [players _]
              (games/player->names players)))

(rf/reg-sub ::rankings-data
            :<- [::games-live-players]
            :<- [::up-to-games]
            :<- [::players]
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

(rf/reg-sub ::games-live-players
            :<- [::games]
            :<- [::dead-players]

            (fn [[games dead-players]]
              (let [inner (fn [field v] (not (contains? dead-players (field v))))]
                (filter #(and (inner :p1 %) (inner :p2 %)) games))))

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

(defn clear-set
  [key]
  (fn [db _]
    (common/assoc-in* db
                      page
                      [key]
                      #{})))

(defn fill-set
  [key reset-fn]
  (fn [db _]
    (common/assoc-in* db
                      page
                      [key]
                      (set (reset-fn db)))))

(defn modify-set
  [key action]
  (fn [db [_ uuid]]
    (let [func (case action
                 :disj disj
                 :conj conj)]

      (common/update-in* db
                         page
                         [key]
                         #(func % uuid)))))

(defn in-set?
  [key]
  (fn [db [_ uuid]]
    (contains? (common/get-in* db page [key]) uuid)))

(rf/reg-sub ::hidden? (in-set? :hidden-players))

(rf/reg-sub ::hidden-players (getter [:hidden-players]))

(rf/reg-event-db ::show (modify-set :hidden-players :disj))

(rf/reg-event-db ::hide (modify-set :hidden-players :conj))

(rf/reg-event-db ::hide-all
                 (fill-set :hidden-players
                           #(set (map :id (common/get-in* % page [:players])))))

(rf/reg-event-db ::show-all (clear-set :hidden-players))

;; dead players
(rf/reg-sub ::dead? (in-set? :dead-players))

(rf/reg-sub ::dead-players (getter [:dead-players]))

(rf/reg-event-db ::revive (modify-set :dead-players :disj))

(rf/reg-event-db ::kill (modify-set :dead-players :conj))

(rf/reg-event-db ::kill-all
                 (fill-set :dead-players
                           #(set (map :id (common/get-in* % page [:players])))))

(rf/reg-event-db ::revive-all (clear-set :dead-players))

(rf/reg-sub ::visible-players
            :<- [::players]
            :<- [::hidden-players]

            (fn [[players hidden-players]]
              (filter #(not (contains? hidden-players (:id %)))
                      players)))

(rf/reg-sub ::highest-rankings-best
            :<- [::rankings-history]

            (fn [history]
              (map second
                   (sort-by
                    (fn [[_ v]]
                      (- (:ranking v)))

                    (medley/map-vals
                     (fn [vs] (last
                               (sort-by :ranking vs)))

                     (group-by :player history))))))

(rf/reg-sub ::rankings-history-vega
            :<- [::rankings-history]

            (fn [history]
              (let [kw->keyname {:player "Player"
                                 :ranking "Ranking"
                                 :game-idx "Game #"
                                 :time "Time"}]

                (map #(set/rename-keys % kw->keyname) history))))

(rf/reg-sub ::best-streaks
            :<- [::results]

            (fn [results]
              (->> results
                   (medley/map-vals games/longest-winning-subseq)
                   (sort-by #(- (second %))))))

(rf/reg-sub ::highest-points
            :<- [::rankings-history]

            (fn [history]
              (->> history
                   (group-by :player)
                   (medley/map-vals #(map :ranking %))
                   (medley/map-vals games/highest-points-subseq)
                   (sort-by #(- (second %))))))

(rf/reg-event-fx ::toggle-show-all
                 (fn [{:keys [db]} _]
                   {:dispatch [::load-games]
                    :db (update-in db
                                   [page :show-all?]
                                   not)}))

(rf/reg-sub ::show-all? (getter [:show-all?]))

(defn winning-percent
  [results]
  (let [freq (frequencies results)
        cent-fn #(int (* 100 (/ (% freq) (count results))))]

    (zipmap [:w :d :l] (map cent-fn [:w :d :l]))))

(rf/reg-sub ::best-percents
            :<- [::results]

            (fn [results]
              (->> results
                   (medley/map-vals winning-percent)
                   (sort-by (comp :l :d :w second)))))
