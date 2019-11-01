(ns byf.league-detail.rankings
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [antizer.reagent :as ant]
            [byf.utils :as utils]
            [reagent.core :as r]
            [byf.league-detail.handlers :as handlers]
            [byf.common.players :as players-handlers]))

(def form-size 7)

(defn el-result
  [idx result]
  [:span
   {:key idx
    :class (str "result__element result__" (name result))}
   (-> result name string/capitalize)])

(defn results-boxes
  [results]
  [:div.result__container
   (map-indexed el-result (take-last form-size results))])

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
            :on-change #(rf/dispatch [::handlers/up-to-games %])}]

          [:span.chevrons
           [ant/button {:on-click #(rf/dispatch [::handlers/prev-game])} "PREV"]
           [:span up-to-current]
           [ant/button {:on-click #(rf/dispatch [::handlers/next-game])} "NEXT"]]]]))))

(defn format-float
  [float-value]
  (string/join "" (take 3 (str float-value))))

(def rankings-columns
  [{:title "position"
    :dataIndex :position}

   {:title "name"
    :dataIndex :name}

   {:title "form"
    :dataIndex :results
    :render (fn [t _ _]
              (r/as-element
               (results-boxes t)))}

   {:title "W"
    :dataIndex :won}

   {:title "D"
    :dataIndex :drawn}

   {:title "L"
    :dataIndex :lost}

   {:title "GF"
    :dataIndex :goals-for}

   {:title "GA"
    :dataIndex :goals-against}

   {:title "GR"
    :dataIndex :goals-ratio}

   {:title "points"
    :dataIndex :ranking
    :render (fn [t _ _] (int t))}])

(defn ->ranking-row
  [results stats name-mapping {:keys [id] :as item}]
  (let [{:keys [wins losses draws points-done points-received]} (get stats id)
        tot-games (+ wins losses draws)
        gf (/ points-done tot-games)
        ga (/ points-received tot-games)]
    (when (pos? tot-games)
      (assoc item
             :name (name-mapping id)
             :results (get results id)
             :won wins
             :lost losses
             :drawn draws
             :goals-for (format-float gf)
             :goals-against (format-float ga)
             :goals-ratio (format-float (/ gf ga))))))

(defn rankings-rows
  []
  (let [name-mapping @(rf/subscribe [::players-handlers/name-mapping])
        results @(rf/subscribe [::handlers/results])
        stats @(rf/subscribe [::handlers/stats])
        sorted-rankings @(rf/subscribe [::handlers/rankings])
        active-players @(rf/subscribe [::players-handlers/active-players])
        filtered-rankings (filter #(active-players (:id %)) sorted-rankings)
        rows (map (fn [i] (->ranking-row results stats name-mapping i)) filtered-rankings)]

    (map-indexed
     (fn [idx item]
       (assoc item :position (inc idx)))
     (filter some? rows))))

(defn rankings-table
  []
  [:div
   [game-slider]
   [ant/table
    {:columns    rankings-columns
     :dataSource (rankings-rows)
     :pagination false
     :loading    false
     :rowKey     :position}]])
