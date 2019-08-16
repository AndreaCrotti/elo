(ns byf.league-detail.rankings
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [byf.utils :as utils]
            [clojure.string :as str]
            [byf.league-detail.handlers :as handlers]
            [byf.common.players :as players-handlers]))

(def form-size 7)

(defn el-result
  [idx result]
  [:span
   {:key idx
    :class (str "result__element result__" (name result))}
   (-> result name str/capitalize)])

(defn results-boxes
  [results]
  [:div.result__container
   (map-indexed el-result (take-last form-size results))])

(defn- enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))

(def hide-show-all
  [:span.hide__show__all
   [:i.fas.fa-eye-slash
    {:title "Hide All"
     :on-click #(rf/dispatch [::handlers/hide-all])}]

   [:i.fas.fa-eye
    {:title "Show All"
     :on-click #(rf/dispatch [::handlers/show-all])}]])

(def kill-revive-all
  [:span
   [:i.fas.fa-skull
    {:title "Kill All"
     :on-click #(rf/dispatch [::handlers/kill-all])}]

   [:i.fas.fa-life-ring
    {:title "Revive All"
     :on-click #(rf/dispatch [::handlers/revive-all])}]])

(defn game-slider
  []
  (let [games (rf/subscribe [::handlers/games-live-players])
        up-to-games (rf/subscribe [::handlers/up-to-games])]

    (fn []
      (let [up-to-current (if (some? @up-to-games) @up-to-games (count @games))]
        [:div
         [:divs
          [ant/slider
           {:type "range"
            :min 0
            :max (count @games)
            :value up-to-current
            :class "slider"
            :on-change (utils/set-val ::handlers/up-to-games js/parseInt)}]

          [:span.chevrons
           [:i.fas.fa-chevron-left {:on-click #(rf/dispatch [::handlers/prev-game])}]
           [:span up-to-current]
           [:i.fas.fa-chevron-right {:on-click #(rf/dispatch [::handlers/next-game])}]]]]))))

(defn rankings-table
  []
  ;; more logic here should be moved into subscriptions,
  ;; waaaay too many subscriptions in this file already
  (let [name-mapping @(rf/subscribe [::players-handlers/name-mapping])
        results @(rf/subscribe [::handlers/results])
        stats @(rf/subscribe [::handlers/stats])
        sorted-rankings @(rf/subscribe [::handlers/rankings])
        active-players @(rf/subscribe [::players-handlers/active-players])
        filtered-rankings (filter #(active-players (:id %)) sorted-rankings)
        header [:tr.tr
                [:th.th hide-show-all]
                [:th.th kill-revive-all]
                [:th.th "position"]
                [:th.th "player"]
                [:th.th "ranking"]
                [:th.th "form"]
                [:th.th "# W/L/D"]]]

    [:div
     [game-slider]
     [:table
      [:thead header]
      (into [:tbody]
            (for [[idx {:keys [id ranking]}] (enumerate filtered-rankings)

                  :let [{:keys [wins losses draws]} (get stats id)
                        player-name (get name-mapping id)
                        hidden? @(rf/subscribe [::handlers/hidden? id])
                        dead? @(rf/subscribe [::handlers/dead? id])]]

              [:tr.tr {:class (if dead? "dead__ranking__row" "alive__ranking__row")}
               [:td.td
                [:span
                 (if hidden?
                   [:i.fas.fa-eye
                    {:title (str "Show " player-name)
                     :on-click #(rf/dispatch [::handlers/show id])}]

                   [:i.fas.fa-eye-slash
                    {:title (str "Hide " player-name)
                     :on-click #(rf/dispatch [::handlers/hide id])}])]]

               [:td.td
                [:span
                 (if dead?
                   [:i.fas.fa-life-ring
                    {:title (str "Revive " player-name)
                     :on-click #(rf/dispatch [::handlers/revive id])}]

                   [:i.fas.fa-skull
                    {:title (str "Kill " player-name)
                     :on-click #(rf/dispatch [::handlers/kill id])}])]]

               [:td.td idx]
               [:td.td player-name]
               [:td.td (int ranking)]
               #_[:td
                  (when (contains? last-changes player-name)
                    (int (get last-changes player-name)))]
               [:td.td (results-boxes (get results id))]
               [:td.td (str wins "/" losses "/" draws)]]))]]))
