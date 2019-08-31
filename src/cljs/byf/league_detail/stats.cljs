(ns byf.league-detail.stats
  (:require [re-frame.core :as rf]
            [antizer.reagent :as ant]
            [clojure.set :refer [rename-keys]]
            [byf.specs.stats :as stats-specs]
            [byf.common.players :as players-handlers]
            [byf.league-detail.handlers :as handlers]
            [byf.league-detail.utils :refer [format-date]]
            [clojure.spec.alpha :as s]))

(def stats-length 5)

(defn- truncate-float
  [v]
  (if (float? v) (int v) v))

(defn- percent
  [v]
  (str (int v) " %"))

(def stats-config
  {::stats-specs/highest-ranking
   {:handler ::handlers/highest-rankings-best
    :title "Highest Score"
    :fields [{:k :player :v "name"} {:k :ranking :v "ranking"} {:k :time :v "time"}]
    :transform {:time format-date :ranking truncate-float}}

   ::stats-specs/longest-winning-streak
   {:handler ::handlers/longest-winning-streaks
    :title "Longest Winning Streak"
    :fields [{:k :player :v "name"} {:k :streak :v "streak"}]}

   ::stats-specs/longest-unbeaten-streak
   {:handler ::handlers/longest-unbeaten-streaks
    :title "Longest Unbeaten Streak"
    :fields [{:k :player :v "name"} {:k :streak :v "streak"}]}

   ::stats-specs/highest-increase
   {:handler ::handlers/highest-increase
    :title "Highest Points increase"
    :fields [{:k :player :v "name"} {:k :points :v "points"}]
    :transform {:points truncate-float}}

   ::stats-specs/best-percents
   {:handler ::handlers/best-percents
    :title "Best Winning %"
    :fields [{:k :player :v "name"} {:k :w :v "win %"}
             {:k :d :v "draw %"} {:k :l :v "loss %"}]

    :transform {:w percent :d percent :l percent}}})

(defn to-column-defs
  [stats-key]
  (->> stats-config
       stats-key
       :fields
       (map #(rename-keys % {:k :dataIndex :v :title}))))

(defn- transform-row
  [data tr]
  (reduce-kv update data tr))

(defn stats-table
  [columns rows]
  [ant/table
   {:columns columns
    :dataSource rows
    :size "small"
    :pagination false
    :loading false
    :bordered true}])

(defn stats-component
  [kw]
  (let [{:keys [handler transform title]} (kw stats-config)
        stats @(rf/subscribe [handler])
        active-player-names @(rf/subscribe [::players-handlers/active-players-names])
        filtered-stats (take stats-length
                             (filter #(active-player-names (:player %)) stats))
        transformed (map
                     #(transform-row % (or transform {}))
                     filtered-stats)]

    (s/assert kw stats)
    [ant/card
     [:label.title title]
     [stats-table (to-column-defs kw) transformed]]))
