(ns byf.league-detail.views
  (:require [antizer.reagent :as ant]
            [byf.auth :as auth]
            [byf.common.players :as players-handlers]
            [byf.common.views :as common-views]
            [byf.league-detail.games-list :refer [games-table]]
            [byf.league-detail.add-game :refer [game-form]]
            [byf.league-detail.handlers :as handlers]
            [byf.vega :as vega]
            [byf.league-detail.stats :refer [stats-component]]
            [byf.specs.stats :as stats-specs]
            [byf.league-detail.rankings :refer [rankings-table]]
            [byf.league-detail.utils :as utils]
            [byf.league-detail.notifications :refer [add-user-notification]]
            [cljsjs.moment]
            [re-frame.core :as rf]))

(def timestamp-format "YYYY-MM-DDZhh:mm:SS")
(def vega-last-n-games 200)

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
        to-game (rf/subscribe [::handlers/to-game])
        norm-from (or @from-game 0)
        norm-to (or @to-game (count @history))
        filtered-history (from-to @history norm-from norm-to)
        last-only (take-last vega-last-n-games filtered-history)]
    [ant/card
     [ant/button
      {:on-click #(rf/dispatch [::handlers/toggle-graph])}
      (if @show-graph
        "hide graph"
        "show graph")]

     (when @show-graph
       [ant/card
        [vega/vega-inner last-only @rankings-domain]])]))

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
   ["graphs" "GRAPHS"]
   ["games" "GAMES"]])

(defn navbar
  []
  (let [user @(rf/subscribe [:user])]
    [ant/layout-header
     [ant/menu {:theme "dark" :mode "horizontal"}
      (conj (for [[k s] menu-config]
              [ant/menu-item
               [:a {:href (utils/update-fragment js/window.location.href k)
                    :on-click #(rf/dispatch [::handlers/set-current-page (keyword k)])}
                s]])
            [ant/menu-item
             (js/console.log (str user))
             (when-not (nil? user)
               [:a {:on-click #(rf/dispatch [:sign-out])}
                (str "Sign Out: " (:display-name user))])])]]))

(defn stats-tab
  []
  [:div {:id "stats"}
   [stats-component ::stats-specs/highest-ranking]
   [stats-component ::stats-specs/longest-winning-streak]
   [stats-component ::stats-specs/longest-unbeaten-streak]
   [stats-component ::stats-specs/highest-increase]
   [stats-component ::stats-specs/best-percents]])

(defn tabs
  []
  (let [loading? (rf/subscribe [::handlers/loading?])
        errors   (rf/subscribe [:failed])
        page     (rf/subscribe [::handlers/current-page])]

    (if (auth/logged-out?)
      [auth/authenticate]
      [:div.root
       [navbar]
       (if @errors
         [common-views/errors]
         [ant/layout-content
          (if @loading?
            [ant/spin {:size "large"}]
            [:div.content
             (case @page
               :add-game [:div {:id "add-game"}
                          [game-form]
                          [add-user-notification]]
               :rankings [:div {:id "rankings"}
                          [rankings-table]]
               :graphs   [vega-outer]
               :stats    [stats-tab]
               :games    [:div {:id "games"}
                          [ant/card
                           [games-table]]])])])

       [common-views/footer]])))

(defn root
  []
  ;; this is kind of an antipattern for reframe
  (rf/dispatch [::handlers/load-league])
  (rf/dispatch [::handlers/load-games])
  (rf/dispatch [::players-handlers/load-players])
  [tabs])
