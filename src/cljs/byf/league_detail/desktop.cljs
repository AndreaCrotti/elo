(ns byf.league-detail.desktop
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
            [vega/vega-inner filtered-history @rankings-domain]])]))))

(defn mobile?
  []
  (js/console.log "avail width " js/window.screen.availWidth)
  (< js/window.screen.availWidth 500))

(defn results
  []
  (rf/dispatch [::handlers/load-games])
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
          [:div {:id "rankings"}
           [rankings-table]]
          [vega-outer]
          [:div {:id "stats"}
           [stats-component ::stats-specs/highest-ranking]
           [stats-component ::stats-specs/longest-winning-streak]
           [stats-component ::stats-specs/longest-unbeaten-streak]
           [stats-component ::stats-specs/highest-increase]
           [stats-component ::stats-specs/best-percents]]
          [:div {:id "games"}
           [ant/card
            [games-table]]]])])))

(defn set-current-user
  "Set the current user to something, defaulting to the already set user?"
  []
  (let [players (rf/subscribe [::players-handlers/players])
        sorted-players (sort-by :name @players)
        current-user @(rf/subscribe [::handlers/current-user])]

    [ant/form {:layout "inline"}
     [ant/form-item
      [ant/button
       {:on-click #(rf/dispatch [::handlers/store-current-user current-user])}
       "Remember Me"]]

     [ant/form-item
      [common-views/drop-down-players sorted-players
       ::handlers/set-current-user current-user]]]))

(defn go-to-internal
  [place]
  (set! (.-hash js/location) place))

(def menu-config
  [["add-game" "NEW GAME"]
   ["rankings" "RANKINGS"]
   ["stats" "STATS"]
   ["games" "GAMES"]])

(defn navbar
  []
  (let [league-name @(rf/subscribe [::handlers/league-name])]
    [ant/layout-header
     (into [ant/menu {:theme "dark"
                      :mode "horizontal"}]

           (concat [[ant/menu-item league-name]
                    [ant/menu-item [:a {:href "/"} "ALL LEAGUES"]]]
                   (for [[k s] menu-config
                         :let [hashed (str "#" k)]]
                     [ant/menu-item [:a {:on-click #(go-to-internal hashed)}
                                     s]])))]))

(defn root
  []
  ;; this is kind of an antipattern for reframe
  (rf/dispatch [::handlers/load-league])
  (rf/dispatch [::players-handlers/load-players])

  (let [loading? @(rf/subscribe [::handlers/loading?])
        errors @(rf/subscribe [:failed])]
    [:div.root
     [navbar]

     (if errors
       [common-views/errors]
       [ant/layout-content
        (if loading?
          [ant/spin {:size "large"}]
          [:div.content
           #_[ant/card
              [set-current-user]]
           [current-user-notification]
           [:div {:id "add-game"}
            [game-form]]
           [add-user-notification]
           [results]])])
     [common-views/footer]]))
