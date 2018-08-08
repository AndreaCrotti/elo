(ns elo.views
  (:require [re-frame.core :as rf]
            [cljsjs.moment]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")

(defn- set-val
  [handler-key]
  #(rf/dispatch [handler-key (-> % .-target .-value)]))

(defn- drop-down
  [opts key]
  (into [:select.form-control {:on-change (set-val key)}]
        (for [o opts]
          [:option {:value o} o])))

(defn- drop-down-players
  [players key]
  (into [:select.form-control {:on-change (set-val key)}]
        (for [p players]
          [:option {:value (:id p)} (:name p)])))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn players-form
  [players]
  [:form.form-group.players_form
   [:div
    [:label {:for "p1"} "Player 1"]
    [drop-down-players players :p1]]

   [:div
    [:label {:for "p2_name"} "Player 2"]
    [drop-down-players players :p2_name]]

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

   [:button.submit__game.btn.btn-primary {:type "submit"
                                          :on-click #(rf/dispatch [:submit])}

    "Submit"]])

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

    [:table.table
     [:thead header]
     (into [:tbody]
           (for [{:keys [p1 p2 p1_team p2_team p1_goals p2_goals played_at]} games]
             [:tr
              [:td (:name (name-mapping p1))]
              [:td p1_team]
              [:td p1_goals]
              [:td (:name (name-mapping p2))]
              [:td p2_team]
              [:td p2_goals]
              [:td played_at]]))]))

(defn rankings-table
  [rankings name-mapping]
  (let [header [:tr [:th "Position"] [:th "Player"] [:th "Ranking"]]
        sorted (sort-by #(- (second %)) rankings)]

    [:table.table
     [:thead header]
     (into [:tbody]
           (for [n (range (count sorted))]
             (let [[p ranking] (nth sorted n)]
               [:tr
                [:td (inc n)]
                [:td (:name (name-mapping p))]
                [:td (int ranking)]])))]))

(defn root
  []
  (rf/dispatch [:load-games])
  (rf/dispatch [:load-rankings])
  (rf/dispatch [:load-players])

  (let [rankings (rf/subscribe [:rankings])
        games (rf/subscribe [:games])
        players (rf/subscribe [:players])]

    (fn []
      (let [name-mapping (into {} (for [p @players] p))]
        [ui/mui-theme-provider
         {:mui-theme (get-mui-theme
                      {:palette {:text-color (color :green600)}})}

         [:div.content
          [:a {:href "https://github.com/AndreaCrotti/elo"}
           [:img.fork-me {:src "https://s3.amazonaws.com/github/ribbons/forkme_right_gray_6d6d6d.png"
                          :alt "Fork me on Github"}]]

          [:div.players__form_container (players-form @players)]
          [:div.rankings__table (rankings-table @rankings name-mapping)]
          [:div.games__table (games-table @games name-mapping)]]]))))
