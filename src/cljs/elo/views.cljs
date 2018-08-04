(ns elo.views)

(defn- drop-down
  [opts]
  (into [:select]
        (for [o opts]
          [:option {:value o} o])))

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

   [:input {:type "text" :placeholder "Team Name"}]
   [:input {:type "text" :placeholder "Team Name"}]

   [:input {:type "submit"} "Submit Result"]])

(defn root
  []
  [:div players-form])
