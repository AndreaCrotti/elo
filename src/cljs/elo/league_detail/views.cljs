(ns elo.league-detail.views
  (:require [accountant.core :as accountant]
            [cljsjs.moment]
            [clojure.string :as str]
            [elo.common.views :refer [drop-down]]
            [elo.date-picker-utils :refer [date-time-picker]]
            [elo.league-detail.handlers :as handlers]
            [elo.routes :as routes]
            [elo.shared-config :as config]
            [elo.utils :as utils]
            [re-frame.core :as rf]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")
(def form-size 5)

(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])

(defn- translate
  [term]
  (let [league (rf/subscribe [::handlers/league])]
    ;;XXX: is there a way to avoid all this extra safety?
    (config/term (or (:game_type @league) :fifa) term)))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn date-range-picker
  []
  (let [game (rf/subscribe [::handlers/game])]
    [:div.filter-panel--range__inputs.date-range__inputs
     [date-time-picker {:name "datetime-widget"
                        :selected (:played_at @game)
                        :react-key "date-picker"
                        :date (js/moment)
                        :min-date "2018-08-01"
                        :max-date (js/moment)
                        :placeholder "When was it played"
                        :on-change #(rf/dispatch [::handlers/played_at %])
                        :class "date-picker-class"}]]))

(defn game-form
  []
  (let [players (rf/subscribe [::handlers/players])
        valid-game? (rf/subscribe [::handlers/valid-game?])
        game (rf/subscribe [::handlers/game])
        league (rf/subscribe [::handlers/league])
        game-type (or (:game_type @league) :fifa)
        points-range (map str (config/opts game-type :points))
        ;; with two different players list we can filter out directly
        ;; the one that was already selected
        sorted-players (sort-by :name @players)]

    [:div.form-group.game_form {:on-submit (fn [] false)}
     [:div
      [:label {:for "p1_name"} "Player 1"]
      [drop-down-players sorted-players ::handlers/p1 (:p1 @game)]]

     [:div
      [:label {:for "p2_name"} "Player 2"]
      [drop-down-players sorted-players ::handlers/p2 (:p2 @game)]]

     [:div
      [:label {:for "p1_points"} (str "# " (translate :points))]
      [drop-down points-range ::handlers/p1_points (:p1_points @game)]]

     [:div
      [:label {:for "p2_points"} (str "# " (translate :points))]
      [drop-down points-range ::handlers/p2_points (:p2_points @game)]]

     [:div
      [:label (translate :using)]
      [:input.form-control {:type "text"
                            :placeholder (str (translate :using) " Name")
                            :value (:p1_using @game)
                            :on-change (utils/set-val ::handlers/p1_using)}]]

     [:div
      [:label (translate :using)]
      [:input.form-control {:type "text"
                            :placeholder (str (translate :using) " Name")
                            :value (:p2_using @game)
                            :on-change (utils/set-val ::handlers/p2_using)}]]

     [:div
      [:label "Played at"]
      [date-range-picker]]

     [:div
      [:button {:type "button"
                :class (utils/classes ["submit__game" "btn" "btn-primary" (when-not @valid-game? "disabled")])
                :on-click (if @valid-game?
                            #(rf/dispatch [::handlers/add-game])
                            #(js/alert "Invalid results or incomplete form"))}

       "Add Game"]]]))

(defn- enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))

(defn change-status
  [uuid]
  (let [dead? @(rf/subscribe [::handlers/dead uuid])
        action (if dead? ::handlers/resuscitate-player ::handlers/kill-player)
        caption (if dead? "resuscitate!" "kill!")]

    [:button {:type "button"
              :class "btn btn-secondary"
              :on-click #(rf/dispatch [action uuid])}

     caption]))

(defn live-players
  []
  (let [players @(rf/subscribe [::handlers/players])
        name-mapping @(rf/subscribe [::handlers/name-mapping])]

    [:table.table.table-striped
     [:thead [:tr [:th "dead?"] [:th "name"]]]
      (into [:tbody]
            (for [{:keys [id]} players]
              [:tr
               [:td [change-status id]]
               [:td (get name-mapping id)]]))]))

(defn games-table
  []
  (let [games @(rf/subscribe [::handlers/games-live-players])
        name-mapping @(rf/subscribe [::handlers/name-mapping])
        up-to (rf/subscribe [::handlers/up-to-games])
        first-games (if (some? @up-to)
                      (take @up-to games)
                      games)
        header [:tr
                [:th "game #"]
                [:th "player 1"]
                [:th (translate :using)]
                [:th (translate :points)]
                [:th "player 2"]
                [:th (translate :using)]
                [:th (translate :points)]
                [:th "played At"]]]

    [:div
     [:h3 "List of Games"]
     [:table.table.table-striped
      [:thead header]
      (into [:tbody]
            (for [[idx {:keys [p1 p2 p1_using p2_using p1_points p2_points played_at]}]
                  (reverse (enumerate first-games))]

              [:tr
               [:td idx]
               [:td (get name-mapping p1)]
               [:td p1_using]
               [:td p1_points]
               [:td (get name-mapping p2)]
               [:td p2_using]
               [:td p2_points]
               [:td (.format (js/moment played_at) "LLLL")]]))]]))

(defn el-result
  [idx result]
  [:span {:key idx :class (str "result__element result__" (name result))}
   (-> result name str/capitalize)])

(defn results-boxes
  [results]
  (map-indexed el-result (take-last form-size results)))

(defn rankings-table
  []
  (let [name-mapping @(rf/subscribe [::handlers/name-mapping])
        results @(rf/subscribe [::handlers/results])
        stats @(rf/subscribe [::handlers/stats])
        header [:tr
                [:th "position"]
                [:th "player"]
                [:th "ranking"]
                [:th "# of games"]
                [:th "form"]
                [:th "# W/L/D"]]
        up-to-games (rf/subscribe [::handlers/up-to-games])
        games (rf/subscribe [::handlers/games-live-players])
        sorted-rankings @(rf/subscribe [::handlers/rankings])
        non-zero-games (filter #(pos? (:ngames %)) sorted-rankings)
        up-to-current (if (some? @up-to-games) @up-to-games (count @games))]

    [:div
     [:h3 "Players Rankings"]
     [:div
      [:span.rankings-slider
       [:input.up-to-range-slider {:type "range"
                                   :min 0
                                   :max (count @games)
                                   :value up-to-current
                                   :class "slider"
                                   :on-change (utils/set-val ::handlers/up-to-games)}]]

      [:span.rankings-chevrons
       [:i.fas.fa-chevron-left {:on-click #(rf/dispatch [::handlers/prev-game])}]
       [:span.up-to-current-games up-to-current]
       [:i.fas.fa-chevron-right {:on-click #(rf/dispatch [::handlers/next-game])}]]]

     [:table.table.table-striped
      [:thead header]
      (into [:tbody]
            (for [[idx {:keys [id ranking ngames]}] (enumerate non-zero-games)
                  :let [{:keys [wins losses draws]} (get stats id)]]
              [:tr
               [:td idx]
               [:td (get name-mapping id)]
               [:td (int ranking)]
               [:td ngames]
               [:td (results-boxes (get results id))]
               [:td (str wins "/" losses "/" draws)]]))]]))

(defn github-fork-me
  []
  [:a {:href "https://github.com/AndreaCrotti/elo"}
   [:img.fork-me {:src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
                  :alt "Fork me on Github"}]])

(defn show-error
  []
  (let [error @(rf/subscribe [::handlers/error])]
    (when error
      [:div.section.alert.alert-danger
       [:pre (:status-text error)]
       [:pre (:original-text error)]])))

(defn preamble
  []
  (let [league @(rf/subscribe [::handlers/league])]
    [:div.preamble
     [:img {:src "/logos/home.png"
            :width "50px"
            :on-click #(accountant/navigate! (routes/path-for :league-list))}]

     (when (some? (:game_type league))
       [:span.league__logo
        [:img {:width "100px"
               :src (config/logo (-> league :game_type))}]])]))

(defn vega
  []
  (rf/dispatch [::handlers/load-graph])
  (let [ts (rf/subscribe [::handlers/timeseries])]
    (fn []
      (js/console.log @ts)
      [:div "hello world"])))

(defn root
  []
  (rf/dispatch [::handlers/load-league])
  (rf/dispatch [::handlers/load-games])
  (rf/dispatch [::handlers/load-players])

  (fn []
    [:div.league_detail__root
     [github-fork-me]
     [show-error]
     [preamble]

     #_[:div.vega-visualization [vega]]
     [:div.section.players__form_container [game-form]]
     [:div.section.players__alive_container [live-players]]
     [:div.section.rankings__table [rankings-table]]
     [:div.section.games__table [games-table]]]))
