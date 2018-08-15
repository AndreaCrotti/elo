(ns elo.views
  (:require [re-frame.core :as rf]
            [cljs-time.core :refer [now]]
            [elo.date-picker-utils :refer [date-time-picker]]
            [cljsjs.moment]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")

(defn smart-dispatch
  [signal]
  (fn [e]
    (rf/dispatch [signal])))

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
  [:div.form-group.register_form
   [:div
    [:input.form-control {:type "text"
                          :placeholder "John Smith"
                          :on-change (set-val :name)}]

    [:input.form-control {:type "text"
                          :placeholder "john.smith@email.com"
                          :on-change (set-val :email)}]]

   [:div
    [:button.submit__game.btn.btn-primary {:type "button"
                                           :on-click (smart-dispatch :add-player)}
     "Register New Player"]]])

(defn date-range-picker
  "Simple date-range picker component.

  Required options:
  - :from & :to - current state for the from and to sides of the picker, if
  using a single date widget only the 'from' argument is used
  - :react-key-prefix - a unique string prefix for internal react-keys
  - :single? - only use a single date widget
  - :placeholder-text - the placeholder text for the single date widget
  - :min-date - Earliest day to select
  - :max-date - Latest day to select

  The :on-change callback is optional. When provided it should be a
  function taking two arguments, side (one of :from, :to) and the new value for
  that side. If using a single date widget, the callback should be one argument:
  the new date value"
  []
  [:div.filter-panel--range__inputs.date-range__inputs
   [date-time-picker {:name "datetime-widget"
                      :react-key "date-picker"
                      :date "2011-01-01"
                      :min-date "2011-01-01"
                      :max-date "2020-01-01"
                      :placeholder "When was it played"
                      :on-change (set-val :played-at)
                      :class "date-picker-class"}]])

(defn players-form
  [players]
  [:div.form-group.players_form {:on-submit (fn [] false)}
   [:div
    [:label {:for "p1"} "Player 1"]
    [drop-down-players players :p1]]

   [:div
    [:label {:for "p2_name"} "Player 2"]
    [drop-down-players players :p2]]

   [:div
    [:label {:for "p1_goals"} "# Goals"]
    [drop-down (map str (range 0 10)) :p1_goals]]

   [:div
    [:label {:for "p2_goals"} "# Goals"]
    [drop-down (map str (range 0 10)) :p2_goals]]

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

   ;; [:label "Played When?"]
   ;; [:input.form-control {:type "datetime-local"
   ;;                       :value (now-format)}]

   [date-range-picker]

   [:button.submit__game.btn.btn-primary {:type "button"
                                          :on-click  (smart-dispatch :add-game)}

    "Add Game"]])

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
        players (rf/subscribe [:players])]

    (fn []
      (let [name-mapping (into {} (for [p @players] {(:id p) p}))]
        [:div.content
         [:a {:href "https://github.com/AndreaCrotti/elo"}
          [:img.fork-me {:src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
                         :alt "Fork me on Github"}]]

         [:div.section.register__form_container (register-form)]
         [:div.section.players__form_container (players-form @players)]
         [:div.section.rankings__table (rankings-table @rankings name-mapping)]
         [:div.section.games__table (games-table @games name-mapping)]]))))
