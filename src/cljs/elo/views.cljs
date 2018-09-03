(ns elo.views
  (:require [re-frame.core :as rf]
            [clojure.string :refer [join]]
            [elo.date-picker-utils :refer [date-time-picker]]
            [elo.shared-config :as config]
            [cljsjs.moment]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")
(def goals-range (map str (range 0 10)))

(defn smart-dispatch
  [signal]
  (fn [e]
    (rf/dispatch [signal])))

(defn- classes
  [cls]
  (join " " (filter some? cls)))

(defn- set-val
  [handler-key]
  #(rf/dispatch [handler-key (-> % .-target .-value)]))

(defn- drop-down
  [opts key value]
  (into [:select.form-control {:on-change (set-val key)
                               :value (or value "")}]
        (cons [:option ""]
              (for [o opts]
                [:option {:value o} o]))))

(defn- drop-down-players
  [players key value]
  (into [:select.form-control {:on-change (set-val key)
                               :value (or value "")}]
        (cons [:option ""]
              (for [p (sort-by :name players)]
                [:option {:value (:id p)} (:name p)]))))

(defn- translate
  [term]
  (let [league (rf/subscribe [:league])]
    ;;XXX: is there a way to avoid all this extra safety?
    (config/term (or (:game_type @league) :fifa) term)))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn add-player-form
  []
  (let [valid-player? (rf/subscribe [:valid-player?])
        player (rf/subscribe [:player])]
    [:div.form-group.add-player_form
     [:div
      [:input.form-control {:type "text"
                            :value (:name @player)
                            :name "name"
                            :placeholder "John Smith"
                            :on-change (set-val :name)}]

      [:input.form-control {:type "text"
                            :value (:email @player)
                            :name "email"
                            :placeholder "john.smith@email.com"
                            :on-change (set-val :email)}]]

     [:div
      [:button {:type "button"
                :name "submit-game"
                :class (classes ["submit__game" "btn" "btn-primary" (when-not @valid-player? "disabled")])
                :on-click (if @valid-player?
                            (smart-dispatch :add-player)
                            #(js/alert "Fill up the form first"))}

       "Register New Player"]]]))

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
  [players]
  (let [valid-game? (rf/subscribe [:valid-game?])
        game (rf/subscribe [:game])
        league (rf/subscribe [:league])
        game-type (or (:game_type @league) :fifa)
        points-range (map str (config/opts game-type :points))]

    [:div.form-group.game_form {:on-submit (fn [] false)}
     [:div
      [:label {:for "p1"} "Player 1"]
      [drop-down-players players :p1 (:p1 @game)]]

     [:div
      [:label {:for "p2_name"} "Player 2"]
      [drop-down-players players :p2 (:p2 @game)]]

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
                            :on-change (set-val :p1_using)}]]

     [:div
      [:label (translate :using)]
      [:input.form-control {:type "text"
                            :placeholder (str (translate :using) " Name")
                            :value (:p2_using @game)
                            :on-change (set-val :p2_using)}]]

     [:div
      [:label "Played at"]
      [date-range-picker]]

     [:div
      [:button {:type "button"
                :class (classes ["submit__game" "btn" "btn-primary" (when-not @valid-game? "disabled")])
                :on-click (if @valid-game?
                            (smart-dispatch :add-game)
                            #(js/alert "Fill up the form first"))}

       "Add Game"]]]))

(defn- enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))

(defn games-table
  [games name-mapping]
  (let [up-to (rf/subscribe [:up-to-games])
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
               [:td (:name (get name-mapping p1))]
               [:td p1_using]
               [:td p1_points]
               [:td (:name (get name-mapping p2))]
               [:td p2_using]
               [:td p2_points]
               [:td (.format (js/moment played_at) "LLLL")]]))]]))

(defn rankings-table
  [name-mapping]
  (let [header [:tr
                [:th "position"]
                [:th "player"]
                [:th "ranking"]
                [:th "# of games"]]
        up-to-games (rf/subscribe [:up-to-games])
        games (rf/subscribe [:games])
        sorted-rankings @(rf/subscribe [:rankings])
        non-zero-games (filter #(pos? (:ngames %)) sorted-rankings)
        up-to-current (if (some? @up-to-games) @up-to-games (count @games))]

    [:div
     [:h3 "Players Rankings"]
     [:div
      [:label {:for "up-to-games"} (str "Compute Rankings up to game #" up-to-current)]
      [:input {:type "range"
               :min 0
               :max (count @games)
               :value up-to-current
               :class "slider"
               :on-change (set-val :up-to-games)}]]

     [:table.table.table-striped
      [:thead header]
      (into [:tbody]
            (for [[idx {:keys [id ranking ngames]}] (enumerate non-zero-games)]
              [:tr
               [:td idx]
               [:td (:name (get name-mapping id))]
               [:td (int ranking)]
               [:td ngames]]))]]))

(defn root
  []
  (rf/dispatch [:load-games])
  (rf/dispatch [:load-players])
  (rf/dispatch-sync [:load-league])

  (let [games (rf/subscribe [:games])
        players (rf/subscribe [:players])
        error (rf/subscribe [:error])
        league (rf/subscribe [:league])]

    (fn []
      (let [name-mapping (into {} (for [p @players] {(:id p) p}))]
        [:div.content
         [:a {:href "https://github.com/AndreaCrotti/elo"}
          [:img.fork-me {:src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
                         :alt "Fork me on Github"}]]

         (when @error
           [:div.section.alert.alert-danger
            [:pre (:status-text @error)]
            [:pre (:original-text @error)]])

         [:div.preamble
          (when (some? (:game_type @league))
            [:span.league__logo
             [:img {:width "100px"
                    :src (config/logo (-> @league :game_type))}]])

          #_[:span.league__title (:name @league)]]

         [:div.section.add-player__form_container (add-player-form)]
         [:div.section.players__form_container (game-form @players)]
         [:div.section.rankings__table (rankings-table name-mapping)]
         [:div.section.games__table (games-table @games name-mapping)]]))))
