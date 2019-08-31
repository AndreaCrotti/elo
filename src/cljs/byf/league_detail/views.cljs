(ns byf.league-detail.views
  (:require [antizer.reagent :as ant]
            [byf.common.players :as players-handlers]
            [byf.common.views :as common-views]
            [byf.league-detail.games-list :refer [games-table]]
            [byf.league-detail.add-game :refer [game-form]]
            [byf.league-detail.handlers :as handlers]
            [byf.league-detail.rankings :refer [rankings-table]]
            [byf.league-detail.notifications :refer [add-user-notification current-user-notification]]
            [byf.league-detail.stats :refer [stats-component]]
            [byf.specs.stats :as stats-specs]
            [byf.utils :as utils]
            [byf.vega :as vega]
            [cljsjs.moment]
            [re-frame.core :as rf]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")
(def vega-last-n-games 20)

(defn now-format
  []
  (.format (js/moment) timestamp-format))

(defn from-to
  [s f t]
  (take (- t f) (drop f s)))

(defn vega-outer
  []
  (let [history (rf/subscribe [::handlers/rankings-history-vega])
        rankings-domain (rf/subscribe [::handlers/rankings-domain])
        show-graph (rf/subscribe [::handlers/show-graph])
        from-game (rf/subscribe [::handlers/from-game])
        to-game (rf/subscribe [::handlers/to-game])]

    (fn []
      (let [norm-from (or @from-game 0)
            norm-to (or @to-game (count @history))
            filtered-history (from-to @history norm-from norm-to)]

        [ant/card
         [ant/button
          {:on-click #(rf/dispatch [::handlers/toggle-graph])}
          (if @show-graph
            "hide graph"
            "show graph")]

         (when @show-graph
           [ant/card
            [vega/vega-inner filtered-history @rankings-domain]
            #_[:label (str "From game " norm-from)]
            #_[ant/slider
             {:type "range"
              :min 0
              :max norm-to
              :value norm-from
              :on-change (utils/set-val ::handlers/from-game js/parseInt)}]

            #_[:label "To Game " norm-to]
            #_[ant/slider
             {:type "range"
              :min norm-from
              :max (count @history)
              :value norm-to
              :on-change (utils/set-val ::handlers/to-game js/parseInt)}]])]))))

(defn results
  []
  (let [show-results (rf/subscribe [::handlers/show-results])]
    (fn []
      [:div.inner
       (when (utils/mobile?)
         [:button.button.is-fullwidth
          {:on-click #(rf/dispatch [::handlers/toggle-results])}
          (if @show-results
            "Hide Results"
            "Show Results")])

       (when (or (not (utils/mobile?)) @show-results)
         [:div.results-content
          [rankings-table]
          [vega-outer]
          [:div.stats
           [stats-component ::stats-specs/highest-ranking]
           [stats-component ::stats-specs/longest-winning-streak]
           [stats-component ::stats-specs/longest-unbeaten-streak]
           [stats-component ::stats-specs/highest-increase]
           [stats-component ::stats-specs/best-percents]]
          [ant/card [games-table]]])])))

(defn set-current-user
  "Set the current user to something, defaulting to the already set user?"
  []
  (let [players (rf/subscribe [::players-handlers/players])
        sorted-players (sort-by :name @players)
        current-user @(rf/subscribe [::handlers/current-user])]

    [ant/form {:layout "inline"}
     [ant/form-item
      [common-views/drop-down-players sorted-players
       ::handlers/set-current-user current-user
       {:caption "Name"}]]

     [ant/form-item
      [ant/button
       {:on-click #(rf/dispatch [::handlers/store-current-user current-user])}
       "Remember Me"]]]))

(defn navbar
  []
  [ant/layout-header
   [ant/menu {:theme "dark"
              :mode "horizontal"}
    [ant/menu-item [:a {:href "/"} "HOME"]]
    [ant/menu-item "Add Game"]
    [ant/menu-item "Rankings"]
    [ant/menu-item "Stats"]
    [ant/menu-item "Games"]]])

(defn footer
  []
  [ant/layout-footer
   [:a {:href "https://github.com/AndreaCrotti/elo"
        :target "_blank"}
    "Fork me on Github"]])

(defn root
  []
  ;; this is kind of an antipattern for reframe
  (rf/dispatch [::handlers/load-league])
  (rf/dispatch [::handlers/load-games])
  (rf/dispatch [::players-handlers/load-players])

  (let [loading? @(rf/subscribe [::handlers/loading?])
        errors @(rf/subscribe [:failed])]
    [:div.root
     [navbar]
     (if errors
       [common-views/errors]
       [ant/layout-content
        (if loading?
          [:div.spinny [ant/spin {:size "large"}]]
          [:div.content
           [ant/card
            [set-current-user]]
           [ant/card
            [current-user-notification]]
           [ant/card
            [game-form]]
           [ant/card
            [add-user-notification]]
           [results]])])
     [footer]]))
