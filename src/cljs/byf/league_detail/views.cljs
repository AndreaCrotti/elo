(ns byf.league-detail.views
  (:require [cljsjs.moment]
            [clojure.string :as str]
            [byf.common.views :as common-views]
            [byf.date-picker-utils :refer [date-time-picker]]
            [byf.league-detail.handlers :as handlers]
            [byf.common.players :as players-handlers]
            [byf.shared-config :as config]
            [byf.utils :as utils]
            [byf.vega :as vega]
            [byf.specs.stats :as stats-specs]
            [clojure.spec.alpha :as s]
            [re-frame.core :as rf]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")
(def form-size 7)
(def vega-last-n-games 20)
(def stats-length 5)

(defn- translate
  [term]
  (let [league (rf/subscribe [::handlers/league])]
    ;;XXX: is there a way to avoid all this extra safety?
    (config/term (or (:game_type @league) :fifa) term)))

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn date-range-picker
  []
  (let [game (rf/subscribe [::handlers/game])]
    [:div
     [date-time-picker {:name "datetime-widget"
                        :selected (:played_at @game)
                        :react-key "date-picker"
                        :date (js/moment)
                        :min-date "2018-08-01"
                        :max-date (js/moment)
                        :placeholder "When was it played"
                        :on-change #(rf/dispatch [::handlers/played_at %])
                        :class "date-picker-class"}]]))

(defn- enable-button
  [valid-game? opts]
  (if valid-game?
    opts
    (assoc opts :disabled "{true}")))

(defn game-form
  []
  (let [players (rf/subscribe [::players-handlers/players])
        valid-game? (rf/subscribe [::handlers/valid-game?])
        game (rf/subscribe [::handlers/game])
        league (rf/subscribe [::handlers/league])
        game-type (or (:game_type @league) :fifa)
        points-range (map str (config/opts game-type :points))
        sorted-players (sort-by :name @players)]

    [:div
     [:div.field
      [:label.label "Player 1"]
      [:div.control
       [common-views/drop-down-players sorted-players ::handlers/p1 (:p1 @game)
        {:caption "Name"}]]]

     [:div.field
      [:label.label "Goals"]
      [:div.control
       [common-views/drop-down points-range ::handlers/p1_points (:p1_points @game)
        {:caption (translate :points)}]]]

     [:div.field
      [:label.label "Team"]
      [:div.control
       [:input.input
        {:type "text"
         :placeholder (str (translate :using) " Name")
         :value (:p1_using @game)
         :on-change (utils/set-val ::handlers/p1_using)}]]]

     [:div.field
      [:label.label "Player 2"]
      [:div.control
       [common-views/drop-down-players sorted-players ::handlers/p2 (:p2 @game)
        {:caption "Name"}]]]

     [:div.field
      [:label.label "Goals"]
      [:div.control
       [common-views/drop-down points-range ::handlers/p2_points (:p2_points @game)
        {:caption (translate :points)}]]]

     [:div.field
      [:label.label "Team"]
      [:div.control
       [:input.input
        {:type "text"
         :placeholder (str (translate :using) " Name")
         :value (:p2_using @game)
         :on-change (utils/set-val ::handlers/p2_using)}]]]

     [:div.field
      [:label.label "Played at"]
      [:div.control {:id "played_at"}
       [date-range-picker]]]

     [:div.field
      [:button.button.is-danger.is-fullwidth.add_game_button
       (enable-button @valid-game?
                      {:on-click #(rf/dispatch [::handlers/add-game])})

       "Add Game"]]]))

(defn- enumerate
  [xs]
  ;; without sorting it only works up to 30 !!
  (sort (zipmap (map inc (range (count xs))) xs)))

(defn- format-date
  [timestamp]
  (.format (js/moment timestamp) "YYYY-MM-DD"))

(defn games-table
  []
  (let [games (rf/subscribe [::handlers/games-live-players])
        name-mapping (rf/subscribe [::players-handlers/name-mapping])
        up-to (rf/subscribe [::handlers/up-to-games])
        show-all? (rf/subscribe [::handlers/show-all?])]

    (fn []
      (let [first-games (if (some? @up-to)
                          (take @up-to @games)
                          @games)

            header [:tr
                    [:th "game #"]
                    [:th "player 1"]
                    [:th (translate :using)]
                    [:th (translate :points)]
                    [:th "player 2"]
                    [:th (translate :using)]
                    [:th (translate :points)]
                    [:th "played At"]]
            rev-games (-> first-games enumerate reverse)
            filtered-games (if @show-all? rev-games (take 10 rev-games))]

        [:div
         [:button.button.is-fullwidth
          {:on-click #(rf/dispatch [::handlers/toggle-show-all])}
          (if @show-all? "show last 10" "show all")]

         [:table.table.is-striped
          [:thead header]
          (into [:tbody]
                (for [[idx {:keys [p1 p2 p1_using p2_using p1_points p2_points played_at]}]
                      filtered-games]

                  [:tr
                   [:td idx]
                   [:td (get @name-mapping p1)]
                   [:td p1_using]
                   [:td p1_points]
                   [:td (get @name-mapping p2)]
                   [:td p2_using]
                   [:td p2_points]
                   [:td (format-date played_at)]]))]]))))

(defn el-result
  [idx result]
  [:span {:key idx :class (str "result__element result__" (name result))}
   (-> result name str/capitalize)])

(defn results-boxes
  [results]
  (map-indexed el-result (take-last form-size results)))

(defn game-slider
  []
  (let [games (rf/subscribe [::handlers/games-live-players])
        up-to-games (rf/subscribe [::handlers/up-to-games])]

    (fn []
      (let [up-to-current (if (some? @up-to-games) @up-to-games (count @games))]
        [:div.section
         [:div.columns
          [:input.slider.column
           {:type "range"
            :min 0
            :max (count @games)
            :value up-to-current
            :class "slider"
            :on-change (utils/set-val ::handlers/up-to-games js/parseInt)}]

          [:span.column.chevrons
           [:i.fas.fa-chevron-left {:on-click #(rf/dispatch [::handlers/prev-game])}]
           [:span up-to-current]
           [:i.fas.fa-chevron-right {:on-click #(rf/dispatch [::handlers/next-game])}]]]]))))

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

(defn- tag
  [t]
  (fn [v] [t (if (float? v) (int v) v)]))

(defn transform
  [data tr]
  (reduce-kv update data tr))

(defn- stats-table
  ([header data tr]
   [:table.table.is-striped
    [:thead.thead
     (into [:tr.tr] (map (tag :th) (map :v header)))]

    (into [:tbody.tbody]
          (for [row data]
            (into [:tr.tr]
                  (->> (map :k header)
                       (select-keys (transform row tr))
                       (vals)
                       (map (tag :td))))))])

  ([header data]
   (stats-table header data {})))

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
        ;; last-changes @(rf/subscribe [::handlers/last-ranking-changes-by-player])
        header [:tr.tr
                [:th.th hide-show-all]
                [:th.th kill-revive-all]
                [:th.th "position"]
                [:th.th "player"]
                [:th.th "ranking"]
                #_[:th.th "last change"]
                [:th.th "form"]
                [:th.th "# W/L/D"]]]

    [:div
     [game-slider]
     [:table.table.is-fullwidth.is-striped
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

(defn navbar
  []
  (let [league @(rf/subscribe [::handlers/league])]
    [:ul.navbar {:role "navigation"}
     [:li.navbar-brand
      [:a.active {:href "#"} (:game_type league)]]

     [:li.navbar-burger.burger
      [:a {:href "http://github.com/AndreaCrotti/elo"}
       "Fork Me"]]]))

(defn vega-outer
  []
  (let [history (rf/subscribe [::handlers/rankings-history-vega])
        rankings-domain (rf/subscribe [::handlers/rankings-domain])
        show-graph (rf/subscribe [::handlers/show-graph])]

    (fn []
      [:div
       [:button.button.is-fullwidth
        {:on-click #(rf/dispatch [::handlers/toggle-graph])}
        (if @show-graph
          "hide graph"
          "show graph")]

       (when @show-graph
         [vega/vega-inner @history @rankings-domain])])))

(defn- percent
  [v]
  (str (int v) " %"))

(def stats
  {::stats-specs/highest-ranking
   {:handler ::handlers/highest-rankings-best
    :fields [{:k :player :v "name"} {:k :ranking :v "ranking"} {:k :time :v "time"}]
    :transform {:time format-date}}

   ::stats-specs/longest-streak
   {:handler ::handlers/longest-streaks
    :fields [{:k :player :v "name"} {:k :streak :v "streak"}]}

   ::stats-specs/highest-increase
   {:handler ::handlers/highest-increase
    :fields [{:k :player :v "name"} {:k :points :v "points"}]}

   ::stats-specs/best-percents
   {:handler ::handlers/best-percents
    :fields [{:k :player :v "name"} {:k :w :v "win %"}
             {:k :d :v "draw %"} {:k :l :v "loss %"}]

    :transform {:w percent :d percent :l percent}}})

(defn stats-component
  [kw]
  (let [{:keys [handler fields transform]} (kw stats)]
    (let [stats (rf/subscribe [handler])
          active-player-names (rf/subscribe [::players-handlers/active-players-names])]

      (fn []
        ;; make the assertion actually blow up as well
        (s/assert kw @stats)
        [:div.column
         [stats-table
          fields
          (take stats-length
                (filter #(@active-player-names (:player %)) @stats))

          (or transform {})]]))))

(defn game-config
  []
  (let [{:keys [k initial-ranking]} @(rf/subscribe [::handlers/game-config])]
    [:div
     [:label (str "K=" k)]
     [:input.slider.is-fullwidth
      {:type "range"
       :min 20
       :max 44
       :value k
       :on-change (utils/set-val ::handlers/k js/parseInt)}]

     [:label (str "initial ranking=" initial-ranking)]
     [:input.slider.is-fullwidth
      {:type "range"
       :min 100
       :max 2000
       :value initial-ranking
       :on-change (utils/set-val ::handlers/initial-ranking js/parseInt)}]]))

(defn notifications
  []
  (let [show-notification (rf/subscribe [::handlers/show-notification])]
    (fn []
      (when @show-notification
        [:div.notification.is-success
         [:button.delete
          {:on-click #(rf/dispatch [::handlers/clear-notification])}]
         "Thank you, your game has been recorded"]))))

(defn root
  []
  ;; this is kind of an antipattern for reframe
  (rf/dispatch [::handlers/load-league])
  (rf/dispatch [::handlers/load-games])
  (rf/dispatch [::players-handlers/load-players])

  (let [loading? (rf/subscribe [::handlers/loading?])]
    (fn []
      (if (not @loading?)
        [:div.loading]
        [:div.content
         [:div.section [game-form]]
         [notifications]
         [:div.inner
          [:div.columns.section
           [stats-component ::stats-specs/highest-ranking]
           [stats-component ::stats-specs/longest-streak]
           [stats-component ::stats-specs/highest-increase]
           [stats-component ::stats-specs/best-percents]]

          [:div.section [vega-outer]]
          [:div.section [rankings-table]]
          [:div.section [games-table]]]]))))
