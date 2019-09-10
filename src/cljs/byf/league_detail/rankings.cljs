(ns byf.league-detail.rankings
  (:require [re-frame.core :as rf]
            [clojure.string :as string]
            [antizer.reagent :as ant]
            [byf.utils :as utils]
            [reagent.core :as r]
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

(defn format-stats
  [{:keys [wins losses draws points-done points-received]}]
  (string/join "/"
               [wins losses draws points-done points-received]))

(def rankings-columns
  [{:title "position"
    :dataIndex :position}

   {:title "name"
    :dataIndex :name}

   {:title "ranking"
    :dataIndex :ranking
    :render (fn [t _ _] (int t))}

   {:title "form"
    :dataIndex :results

    :render (fn [t _ _ ]
              (r/as-element
               (results-boxes t)))}

   #_{:title "# W/L/D/GD/GR"
    :dataIndex :stats
    :render (fn [t b c ]
              (r/as-element
               (into [:div.inner-stats]
                     (for [v (vals t)]
                       [:span v]))))}])

(defn rankings-rows
  []
  (let [name-mapping @(rf/subscribe [::players-handlers/name-mapping])
        results @(rf/subscribe [::handlers/results])
        stats @(rf/subscribe [::handlers/stats])
        sorted-rankings @(rf/subscribe [::handlers/rankings])
        active-players @(rf/subscribe [::players-handlers/active-players])
        filtered-rankings (filter #(active-players (:id %)) sorted-rankings)]

    (map-indexed
     (fn [idx {:keys [id] :as item}]
       (assoc item
              :position idx
              :name (name-mapping id)
              :results (get results id)
              :stats (get stats id)))

     filtered-rankings)))

(defn rankings-table
  []
  [ant/table
   {:id         "rankings"
    :columns    rankings-columns
    :dataSource (rankings-rows)
    :pagination false
    :loading    false
    :rowKey     :position}])
