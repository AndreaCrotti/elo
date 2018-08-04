(ns elo.views
  (:require [re-frame.core :as rf]))

(defn- set-val
  [handler-key]
  #(rf/dispatch [handler-key (-> % .-target .-value)]))

(defn- drop-down
  [opts key]
  (into [:select {:on-change (set-val key)}]
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

        (for [{:keys [p1_name p2_name p1_team p2_team p1_goals p2_goals]}
              (games)]

          [:tr
           [:td p1_name]
           [:td p1_team]
           [:td p1_goals]
           [:td p2_name]
           [:td p2_team]
           [:td p2_goals]])))

(def players-form
  [:form.players_form
   [:div
    [:label {:for "p1_name"} "Player 1"]
    (drop-down ["one" "two"] :p1_name)]

   [:div
    [:label {:for "p2_name"} "Player 2"]
    (drop-down ["one" "two"] :p2_name)]

   [:div
    [:label {:for "p1_goals"} "# Goals"]
    (drop-down (map str (range 0 10)) :p1_goals)]

   [:div
    [:label {:for "p2_goals"} "# Goals"]
    (drop-down (map str (range 0 10)) :p2_goals)]

   [:div
    [:label "Team"]
    [:input {:type "text"
             :placeholder "Team Name"
             :on-change (set-val :p1_team)}]]

   [:div
    [:label "Team"]
    [:input {:type "text"
             :placeholder "Team Name"
             :on-change (set-val :p2_team)}]]

   [:input.submit__game {:type "submit"
                         :on-click #(rf/dispatch [:submit])}]])

(defn root
  []
  (fn []
    (rf/dispatch [:load-games])
    (rf/dispatch [:load-rankings])
    [:div players-form]))
