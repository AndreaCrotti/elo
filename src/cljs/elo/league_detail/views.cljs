(ns elo.league-detail.views
  (:require [cljsjs.moment]
            [elo.routes :as routes]
            [elo.utils :as utils]
            [elo.common.views :refer [drop-down]]
            [accountant.core :as accountant]
            [elo.date-picker-utils :refer [date-time-picker]]
            [elo.shared-config :as config]
            [re-frame.core :as rf]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")

(defn drop-down-players
  [opts dispatch-key value]
  [drop-down opts dispatch-key value :value-fn :id :display-fn :name])

(defn- translate
  [term]
  (let [league (rf/subscribe [:league])]
    ;;XXX: is there a way to avoid all this extra safety?
    (config/term (or (:game_type @league) :fifa) term)))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn date-range-picker
  []
  (let [game (rf/subscribe [:game])]
    [:div.filter-panel--range__inputs.date-range__inputs
     [date-time-picker {:name "datetime-widget"
                        :selected (:played_at @game)
                        :react-key "date-picker"
                        :date (js/moment)
                        :min-date "2018-08-01"
                        :max-date (js/moment)
                        :placeholder "When was it played"
                        :on-change #(rf/dispatch [:played_at %])
                        :class "date-picker-class"}]]))

(defn game-form
  []
  (let [players (rf/subscribe [:players])
        valid-game? (rf/subscribe [:valid-game?])
        game (rf/subscribe [:game])
        league (rf/subscribe [:league])
        game-type (or (:game_type @league) :fifa)
        points-range (map str (config/opts game-type :points))
        ;; with two different players list we can filter out directly
        ;; the one that was already selected
        sorted-players (sort-by :name @players)]

    [:div.form-group.game_form {:on-submit (fn [] false)}
     [:div
      [:label {:for "p1_name"} "Player 1"]
      [drop-down-players sorted-players :p1 (:p1 @game)]]

     [:div
      [:label {:for "p2_name"} "Player 2"]
      [drop-down-players sorted-players :p2 (:p2 @game)]]

     [:div
      [:label {:for "p1_points"} (str "# " (translate :points))]
      [drop-down points-range :p1_points (:p1_points @game)]]

     [:div
      [:label {:for "p2_points"} (str "# " (translate :points))]
      [drop-down points-range :p2_points (:p2_points @game)]]

     [:div
      [:label (translate :using)]
      [:input.form-control {:type "text"
                            :placeholder (str (translate :using) " Name")
                            :value (:p1_using @game)
                            :on-change (utils/set-val :p1_using)}]]

     [:div
      [:label (translate :using)]
      [:input.form-control {:type "text"
                            :placeholder (str (translate :using) " Name")
                            :value (:p2_using @game)
                            :on-change (utils/set-val :p2_using)}]]

     [:div
      [:label "Played at"]
      [date-range-picker]]

     [:div
      [:button {:type "button"
                :class (utils/classes ["submit__game" "btn" "btn-primary" (when-not @valid-game? "disabled")])
                :on-click (if @valid-game?
                            #(rf/dispatch [:add-game])
                            #(js/alert "Fill up the form first"))}

       "Add Game"]]]))

(defn- enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))

(def result-class
  {1 "fa fas-smile-beam"
   0.5 "fa fas-grin-beam-sweat"
   0 "fa fas-sad-cry"})

(defn games-table
  []
  (let [games @(rf/subscribe [:games-enriched])
        name-mapping @(rf/subscribe [:name-mapping])
        up-to (rf/subscribe [:up-to-games])
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
            (for [[idx {:keys [p1 p2
                               p1_using p2_using
                               r1 r2
                               p1_points p2_points
                               played_at]}]

                  (reverse (enumerate first-games))]

              [:tr
               [:td idx]
               [:td {:class (result-class r1)} (:name (get name-mapping p1))]
               [:td p1_using]
               [:td p1_points]
               [:td {:class (result-class r2)} (:name (get name-mapping p2))]
               [:td p2_using]
               [:td p2_points]
               [:td (.format (js/moment played_at) "LLLL")]]))]]))

(defn rankings-table
  []
  (let [name-mapping @(rf/subscribe [:name-mapping])
        header [:tr
                [:th "position"]
                [:th "player"]
                [:th "ranking"]
                [:th "# of games"]]
        up-to-games (rf/subscribe [:up-to-games])
        games (rf/subscribe [:games])
        sorted-rankings @(rf/subscribe [:rankings])
        non-zero-games (filter #(pos? (:ngames %)) sorted-rankings)
        up-to-current (if (some? @up-to-games) @up-to-games (count @games))
        rankings-average @(rf/subscribe [:rankings-average])]

    [:div
     [:h3 "Players Rankings"]
     [:div.players-rankings__root
      #_[:div "Current average = " rankings-average]
      [:div.rankings-chevrons
       [:p "Move to go back and forth in history"]
       [:i.fas.fa-chevron-left {:on-click #(rf/dispatch [:prev-game])}]
       [:span.up-to-current-games up-to-current]
       [:i.fas.fa-chevron-right {:on-click #(rf/dispatch [:next-game])}]]

      [:div
       [:input.up-to-range-slider {:type "range"
                                   :min 0
                                   :max (count @games)
                                   :value up-to-current
                                   :class "slider"
                                   :on-change (utils/set-val :up-to-games)}]]]

     [:table.table.table-striped
      [:thead header]
      (into [:tbody]
            (for [[idx {:keys [id ranking ngames]}] (enumerate non-zero-games)]
              [:tr
               [:td idx]
               [:td (:name (get name-mapping id))]
               [:td (int ranking)]
               [:td ngames]]))]]))

(defn github-fork-me
  []
  [:a {:href "https://github.com/AndreaCrotti/elo"}
   [:img.fork-me {:src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
                  :alt "Fork me on Github"}]])

(defn show-error
  []
  (let [error @(rf/subscribe [:error])]
    (when error
      [:div.section.alert.alert-danger
       [:pre (:status-text error)]
       [:pre (:original-text error)]])))

(defn preamble
  []
  (let [league @(rf/subscribe [:league])]
    [:div.preamble
     [:img {:src "/logos/home.png"
            :width "50px"
            :on-click #(accountant/navigate! (routes/path-for :league-list))}]

     (when (some? (:game_type league))
       [:span.league__logo
        [:img {:width "100px"
               :src (config/logo (-> league :game_type))}]])]))

(defn root
  []
  (rf/dispatch [:load-games])
  (rf/dispatch [:load-players])
  (rf/dispatch [:load-league])

  (fn []
    [:div.content
     [github-fork-me]
     [show-error]
     [preamble]

     [:div.section.players__form_container [game-form]]
     [:div.section.rankings__table [rankings-table]]
     [:div.section.games__table [games-table]]]))
