(ns byf.league-detail.views
  (:require [antizer.reagent :as ant]
            [byf.common.players :as players-handlers]
            [byf.common.views :as common-views]
            [byf.league-detail.handlers :as handlers]
            [byf.league-detail.rankings :refer [rankings-table]]
            [byf.league-detail.stats :refer [stats-component]]
            [byf.league-detail.utils :refer [enumerate format-date]]
            [byf.shared-config :as config]
            [byf.specs.stats :as stats-specs]
            [byf.utils :as utils]
            [byf.vega :as vega]
            [cljsjs.moment]
            [re-frame.core :as rf]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")
(def vega-last-n-games 20)

(defn- translate
  [term]
  (let [league (rf/subscribe [::handlers/league])]
    ;;XXX: is there a way to avoid all this extra safety?
    (config/term (or (:game_type @league) :fifa) term)))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn- enable-button
  [valid-game? opts]
  (if valid-game?
    opts
    (assoc opts :disabled "{true}")))

(defn game-form
  []
  (let [players (rf/subscribe [::players-handlers/players])
        valid-game? (rf/subscribe [::handlers/valid-game?])
        game (rf/subscribe [::handlers/game])
        league (rf/subscribe [::handlers/league])
        game-type (or (:game_type @league) :fifa)
        points-range (map str (config/opts game-type :points))
        sorted-players (sort-by :name @players)]

    [ant/form {:layout "vertical"}
     [ant/form-item {:label "Player 1"}
      [common-views/drop-down-players sorted-players ::handlers/p1 (:p1 @game)
       {:caption "Name"}]]

     [ant/form-item {:label "Goals"}
      [common-views/drop-down points-range ::handlers/p1_points (:p1_points @game)
       {:caption (translate :points)}]]

     [ant/form-item {:label "Team 1"}
      [ant/input-text-area
       {:value (:p1_using @game)
        :on-change (utils/set-val ::handlers/p1_using)}]]

     [ant/form-item {:label "Player 2"}
      [common-views/drop-down-players sorted-players ::handlers/p2 (:p2 @game)
       {:caption "Name"}]]

     [ant/form-item {:label "Goals"}
      [common-views/drop-down points-range ::handlers/p2_points (:p2_points @game)
       {:caption (translate :points)}]]

     [ant/form-item {:label "Team 2"}
      [ant/input-text-area
       {:default-value (str (translate :using) " Name")
        :value (:p2_using @game)
        :on-change (utils/set-val ::handlers/p2_using)}]]

     [ant/form-item {:label "Played At"}
      [ant/date-picker {:show-time true
                        :format "YYYY-MM-DD HH:mm"}]]

     [ant/form-item
      [ant/button
       (enable-button @valid-game?
                      {:on-click #(rf/dispatch [::handlers/add-game])})

       "Add Game"]]]))

(defn games-table
  []
  (let [games (rf/subscribe [::handlers/games-live-players])
        name-mapping (rf/subscribe [::players-handlers/name-mapping])
        up-to (rf/subscribe [::handlers/up-to-games])
        show-all? (rf/subscribe [::handlers/show-all?])]

    (fn []
      (let [first-games (if (some? @up-to)
                          (take @up-to @games)
                          @games)

            header [:tr
                    [:th "game #"]
                    [:th "player 1"]
                    [:th (translate :using)]
                    [:th (translate :points)]
                    [:th "player 2"]
                    [:th (translate :using)]
                    [:th (translate :points)]
                    [:th "played At"]]
            rev-games (-> first-games enumerate reverse)
            filtered-games (if @show-all? rev-games (take 10 rev-games))]

        [:div
         [ant/button
          {:on-click #(rf/dispatch [::handlers/toggle-show-all])}
          (if @show-all? "show last 10" "show all")]

         [:table
          [:thead header]
          (into [:tbody]
                (for [[idx {:keys [p1 p2 p1_using p2_using p1_points p2_points played_at]}]
                      filtered-games]

                  [:tr
                   [:td idx]
                   [:td (get @name-mapping p1)]
                   [:td p1_using]
                   [:td p1_points]
                   [:td (get @name-mapping p2)]
                   [:td p2_using]
                   [:td p2_points]
                   [:td (format-date played_at)]]))]]))))

(defn from-to
  [s f t]
  (take (- t f) (drop f s)))

(defn vega-outer
  []
  (let [history (rf/subscribe [::handlers/rankings-history-vega])
        rankings-domain (rf/subscribe [::handlers/rankings-domain])
        show-graph (rf/subscribe [::handlers/show-graph])
        from-game (rf/subscribe [::handlers/from-game])
        to-game (rf/subscribe [::handlers/to-game])]

    (fn []
      (let [norm-from (or @from-game 0)
            norm-to (or @to-game (count @history))
            filtered-history (from-to @history norm-from norm-to)]

        [:div
         [ant/button
          {:on-click #(rf/dispatch [::handlers/toggle-graph])}
          (if @show-graph
            "hide graph"
            "show graph")]

         (when @show-graph
           [:div.container
            [vega/vega-inner filtered-history @rankings-domain]
            #_[:label (str "From game " norm-from)]
            #_[ant/slider
             {:type "range"
              :min 0
              :max norm-to
              :value norm-from
              :on-change (utils/set-val ::handlers/from-game js/parseInt)}]

            #_[:label "To Game " norm-to]
            #_[ant/slider
             {:type "range"
              :min norm-from
              :max (count @history)
              :value norm-to
              :on-change (utils/set-val ::handlers/to-game js/parseInt)}]])]))))

(defn notification
  [flag content]
  (when flag
    [:div
     [ant/alert {:type "info"
                 :message content}]]))

;; make this more generic to allow different position and different content
(defn add-user-notification
  []
  (let [show-notification (rf/subscribe [::handlers/add-user-notification])]
    (notification @show-notification
                  "Thank you, your game has been recorded")))

(defn current-user-notification
  []
  (let [current-user-set (rf/subscribe [::handlers/current-user-notification])]
    (notification @current-user-set
                  "Thanks, I'll remember it's you next time")))

(defn results
  []
  (let [show-results (rf/subscribe [::handlers/show-results])]
    (fn []
      [:div.inner
       (when (utils/mobile?)
         [:button.button.is-fullwidth
          {:on-click #(rf/dispatch [::handlers/toggle-results])}
          (if @show-results
            "Hide Results"
            "Show Results")])
       (when (or (not (utils/mobile?)) @show-results)
         [:div.results-content
          [:div.columns
           [stats-component ::stats-specs/highest-ranking]
           [stats-component ::stats-specs/longest-winning-streak]
           [stats-component ::stats-specs/longest-unbeaten-streak]
           [stats-component ::stats-specs/highest-increase]
           [stats-component ::stats-specs/best-percents]]

          [:div [vega-outer]]
          [:div [rankings-table]]
          [:div [games-table]]])])))

(defn set-current-user
  "Set the current user to something, defaulting to the already set user?"
  []
  (let [players (rf/subscribe [::players-handlers/players])
        sorted-players (sort-by :name @players)
        current-user @(rf/subscribe [::handlers/current-user])]

    [ant/form {:layout "inline"}
     [ant/form-item
      [common-views/drop-down-players sorted-players
       ::handlers/set-current-user current-user
       {:caption "Name"}]]

     [ant/form-item
      [ant/button
       {:on-click #(rf/dispatch [::handlers/store-current-user current-user])}
       "Remember Me"]]]))

(defn root
  []
  ;; this is kind of an antipattern for reframe
  (rf/dispatch [::handlers/load-league])
  (rf/dispatch [::handlers/load-games])
  (rf/dispatch [::players-handlers/load-players])

  (let [loading? @(rf/subscribe [::handlers/loading?])
        errors @(rf/subscribe [:failed])]
    [:div.super
     (if errors
       [common-views/errors]
       (if loading?
         [:div.loading]
         [:div.content
          [set-current-user]
          [current-user-notification]
          [game-form]
          [add-user-notification]
          [results]]))]))
