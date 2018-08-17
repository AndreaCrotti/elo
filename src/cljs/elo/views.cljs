(ns elo.views
  (:require [re-frame.core :as rf]
            [clojure.string :refer [join]]
            [elo.date-picker-utils :refer [date-time-picker]]
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
  [opts key]
  (into [:select.form-control {:on-change (set-val key)}]
        (cons [:option ""]
              (for [o opts]
                [:option {:value o} o]))))

(defn- drop-down-players
  [players key]
  (into [:select.form-control {:on-change (set-val key)}]
        (cons [:option ""]
              (for [p players]
                [:option {:value (:id p)} (:name p)]))))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn register-form
  []
  (let [valid-player? (rf/subscribe [:valid-player?])
        email (rf/subscribe [:email])
        name (rf/subscribe [:name])]
    [:div.form-group.register_form
     [:div
      [:input.form-control {:type "text"
                            :value @name
                            :placeholder "John Smith"
                            :on-change (set-val :name)}]

      [:input.form-control {:type "text"
                            :value @email
                            :placeholder "john.smith@email.com"
                            :on-change (set-val :email)}]]

     [:div
      [:button {:type "button"
                :class (classes ["submit__game" "btn" "btn-primary" (when-not @valid-player? "disabled")])
                :on-click (if @valid-player?
                            (smart-dispatch :add-player)
                            #(js/alert "Fill up the form first"))}
       "Register New Player"]]]))

(defn date-range-picker
  []
  [:div.filter-panel--range__inputs.date-range__inputs
   [date-time-picker {:name "datetime-widget"
                      :react-key "date-picker"
                      :date (js/moment)
                      :min-date "2018-08-01"
                      :max-date (js/moment)
                      :placeholder "When was it played"
                      :on-change  (fn [moment]
                                    (rf/dispatch [:played_at (.format moment timestamp-format)]))
                      :class "date-picker-class"}]])

(defn game-form
  [players]
  (let [valid-game? (rf/subscribe [:valid-game?])]
    [:div.form-group.game_form {:on-submit (fn [] false)}
     [:div
      [:label {:for "p1"} "Player 1"]
      [drop-down-players players :p1]]

     [:div
      [:label {:for "p2_name"} "Player 2"]
      [drop-down-players players :p2]]

     [:div
      [:label {:for "p1_goals"} "# Goals"]
      [drop-down goals-range :p1_goals]]

     [:div
      [:label {:for "p2_goals"} "# Goals"]
      [drop-down goals-range :p2_goals]]

     [:div
      [:label "Team"]
      [:input.form-control {:type "text"
                            :placeholder "Team Name"
                            :on-change (set-val :p1_team)}]]

     [:div
      [:label "Team"]
      [:input.form-control {:type "text"
                            :placeholder "Team Name"
                            :on-change (set-val :p2_team)}]]

     [:div {:class "hello world"}
      [:label "Played at"]
      [date-range-picker]]

     [:div
      [:button {:type "button"
                :class (classes ["submit__game" "btn" "btn-primary" (when-not @valid-game? "disabled")])
                :on-click (if @valid-game?
                            (smart-dispatch :add-game)
                            #(js/alert "Fill up the form first"))}

       "Add Game"]]]))

(defn games-table
  [games name-mapping]
  (let [header [:tr
                [:th "Player 1"]
                [:th "Team"]
                [:th "Goals"]
                [:th "Player 2"]
                [:th "Team"]
                [:th "Goals"]
                [:th "Played At"]]]

    [:div
     [:h3 "List of Games"]
     [:table.table
      [:thead header]
      (into [:tbody]
            (for [{:keys [p1 p2 p1_team p2_team p1_goals p2_goals played_at]} games]
              [:tr
               [:td (:name (get name-mapping p1))]
               [:td p1_team]
               [:td p1_goals]
               [:td (:name (get name-mapping p2))]
               [:td p2_team]
               [:td p2_goals]
               [:td played_at]]))]]))

(defn rankings-table
  [rankings name-mapping]
  (let [header [:tr
                [:th "Position"]
                [:th "Player"]
                [:th "Ranking"]
                [:th "# Of Games"]]
        sorted (sort-by #(- (second %)) rankings)]

    [:div
     [:h3 "Players Rankings"]
     [:table.table
      [:thead header]
      (into [:tbody]
            (for [n (range (count sorted))]
              (let [{:keys [id ranking ngames]} (nth sorted n)]
                [:tr
                 [:td (inc n)]
                 [:td (:name (get name-mapping id))]
                 [:td (int ranking)]
                 [:td ngames]])))]]))

(defn root
  []
  (rf/dispatch [:load-games])
  (rf/dispatch [:load-rankings])
  (rf/dispatch [:load-players])

  (let [rankings (rf/subscribe [:rankings])
        games (rf/subscribe [:games])
        players (rf/subscribe [:players])
        ]

    (fn []
      (let [name-mapping (into {} (for [p @players] {(:id p) p}))]
        [:div.content
         [:a {:href "https://github.com/AndreaCrotti/elo"}
          [:img.fork-me {:src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
                         :alt "Fork me on Github"}]]

         [:div.section.register__form_container (register-form)]
         [:div.section.players__form_container (game-form @players)]
         [:div.section.rankings__table (rankings-table @rankings name-mapping)]
         [:div.section.games__table (games-table @games name-mapping)]]))))
