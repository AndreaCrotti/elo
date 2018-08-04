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
    (drop-down ["one" "two"] :p1-name)]

   [:div
    [:label {:for "p2-name"} "Player 2"]
    (drop-down ["one" "two"] :p2-name)]

   [:div
    [:label {:for "p1-goals"} "# Goals"]
    (drop-down (map str (range 0 10)) :p1-goals)]

   [:div
    [:label {:for "p2-goals"} "# Goals"]
    (drop-down (map str (range 0 10)) :p2-goals)]

   [:div
    [:label "Team"]
    [:input {:type "text"
             :placeholder "Team Name"
             :on-change (set-val :p1-team)}]]

   [:div
    [:label "Team"]
    [:input {:type "text"
             :placeholder "Team Name"
             :on-change (set-val :p2-team)}]]

   [:input.submit__game {:type "submit"
                         :on-click #(rf/dispatch [:submit])}]])

(defn root
  []
  [:div players-form])
