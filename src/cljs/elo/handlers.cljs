(ns elo.handlers
  (:require [ajax.core :as ajax]
            [cemerick.url :refer [url]]
            [cljsjs.moment]
            [day8.re-frame.http-fx]
            [elo.games :as games]
            [elo.shared-config :as shared]
            [re-frame.core :as rf]))

(defn- get-league-id
  []
  (-> (url js/window.location.href)
      :query
      (get "league_id")))

;;TODO: this might get defined too late anyway
(def default-game
  {:p1 ""
   :p2 ""
   :p1_points ""
   :p2_points ""
   :p1_using ""
   :p2_using ""
   :played_at (js/moment)})

(def default-player
  {:name ""
   :email ""})

(def default-db
  {:games []
   :players []
   :game {}
   :player {}
   :error nil
   :up-to-games nil
   :league_id (get-league-id)})

(defn- getter
  [ks]
  (fn [db _]
    (get-in db ks)))

(defn- setter
  [key]
  (fn [db [_ val]]
    (assoc-in db key val)))

(rf/reg-sub :rankings
            (fn [query-v _]
              [(rf/subscribe [:games])
               (rf/subscribe [:players])
               (rf/subscribe [:up-to-games])])

            (fn [[games players up-to-games] _]
              (let [rankings
                    (games/get-rankings (if (some? up-to-games)
                                          (take up-to-games games)
                                          games)
                                        players)]

                (sort-by #(- (second %)) rankings))))

(rf/reg-sub :error (getter [:error]))

(rf/reg-sub :valid-game?
            (fn [db _]
              (not-any? #(= % "")
                        (vals (:game db)))))

(rf/reg-sub :valid-player?
            (fn [db _]
              (not-any? #(= % "")
                        (vals (:player db)))))


(rf/reg-event-db :reset-player (fn [db _]
                                 (assoc db :player default-player)))

(rf/reg-event-db :reset-game (fn [db _]
                               (assoc db :game default-game)))

(rf/reg-sub :player (getter [:player]))
(rf/reg-sub :game (getter [:game]))
(rf/reg-sub :up-to-games
            (fn [db _]
              (some-> (:up-to-games db)
                      js/parseInt)))

(rf/reg-sub :league
            (fn [db _]
              (update (:league db) :game_type keyword)))

(rf/reg-sub :games (getter [:games]))
(rf/reg-sub :players (getter [:players]))

(rf/reg-event-db :initialize-db
                 (fn [db _]
                   (assoc default-db
                          :game
                          default-game
                          :player
                          default-player)))

(rf/reg-event-db :p1 (setter [:game :p1]))
(rf/reg-event-db :p1_points (setter [:game :p1_points]))
(rf/reg-event-db :p1_using (setter [:game :p1_using]))
(rf/reg-event-db :up-to-games (setter [:up-to-games]))

(rf/reg-event-db :p2 (setter [:game :p2]))
(rf/reg-event-db :p2_points (setter [:game :p2_points]))
(rf/reg-event-db :p2_using (setter [:game :p2_using]))

(rf/reg-event-db :played_at (setter [:game :played_at]))

(rf/reg-event-db :name (setter [:player :name]))
(rf/reg-event-db :email (setter [:player :email]))


(defn reload-fn-gen
  [extra-signal]
  (fn [{:keys [db]} _]
    (js/alert "Thanks you, results and rankings are updated immediately")
    ;;TODO: would be nice to trigger a transaction of the interested
    ;;area of the page to make it clear what was actually changed
    {:db db
     :dispatch-n (cons extra-signal [[:load-players]
                                     [:load-games]])}))

(rf/reg-event-fx :add-game-success (reload-fn-gen [:reset-game]))
(rf/reg-event-fx :add-player-success (reload-fn-gen [:reset-player]))

(rf/reg-event-db :failed
                 (fn [db [_ {:keys [status parse-error] :as req}]]
                   (js/console.log "Failed request " parse-error "req" req)
                   (assoc db
                          :error
                          {:status status
                           :status-text (:status-text parse-error)
                           :original-text (:original-text parse-error)})))

(rf/reg-event-db :load-games-success (setter [:games]))
(rf/reg-event-db :load-players-success (setter [:players]))
(rf/reg-event-db :load-league-success (setter [:league]))

(defn- loader
  [uri on-success]
  (fn [{:keys [db]} _]
    {:db db
     :http-xhrio {:method :get
                  :uri uri
                  :params {:league_id (:league_id db)}
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(rf/reg-event-fx :load-games (loader "/games" :load-games-success))
(rf/reg-event-fx :load-players (loader "/players" :load-players-success))
(rf/reg-event-fx :load-league (loader "/league" :load-league-success))

(defn writer
  [uri on-success params-fn]
  (fn [{:keys [db]} _]
    {:db db
     :http-xhrio {:method :post
                  :uri uri
                  :params (merge (params-fn db)
                                 {:league_id (:league_id db)})
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success [on-success]
                  :on-failure [:failed]}}))

(defn game-transform
  [db]
  (update (:game db)
          :played_at
          #(.format % shared/timestamp-format)))

(rf/reg-event-fx :add-game (writer "/add-game" :add-game-success game-transform))
(rf/reg-event-fx :add-player (writer "/add-player" :add-player-success :player))
