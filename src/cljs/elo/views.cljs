(ns elo.views)

(defn- drop-down
  [opts]
  (into [:select]
        (for [o opts]
          [:option {:value o} o])))

(defn games-table
  [games]
  (into [:table
         [:th
          [:td "Player 1"]
          [:td "Team"]
          [:td "Goals"]
          [:td "Player 2"]
          [:td "Team"]
          [:td "Goals"]]]

        (for [{:keys [p1-name p2-name p1-team p2-team p1-goals p2-goals]}
              (games)]

          [:tr
           [:td p1-name]
           [:td p1-team]
           [:td p1-goals]
           [:td p2-name]
           [:td p2-team]
           [:td p2-goals]])))

(def players-form
  [:form.players_form
   [:div
    [:label {:for "p1-name"} "Player 1"]
    (drop-down ["one" "two"])]

   [:div
    [:label {:for "p2-name"} "Player 2"]
    (drop-down ["one" "two"])]

   [:div
    [:label {:for "p1-goals"} "# Goals"]
    (drop-down (map str (range 0 10)))]

   [:div
    [:label {:for "p2-goals"} "# Goals"]
    (drop-down (map str (range 0 10)))]

   [:div
    [:label "Team"]
    [:input {:type "text" :placeholder "Team Name"}]]

   [:div
    [:label "Team"]
    [:input {:type "text" :placeholder "Team Name"}]]

   [:input.submit__game {:type "submit"}]])

(defn root
  []
  [:div players-form])
