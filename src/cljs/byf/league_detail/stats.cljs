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

(defn- percent
  [v]
  (str (int v) " %"))

(def stats-config
  {::stats-specs/highest-ranking
   {:handler ::handlers/highest-rankings-best
    :title "Highest Score"
    :fields [{:k :player :v "name"} {:k :ranking :v "ranking"} {:k :time :v "time"}]
    :transform {:time format-date}}

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
    :fields [{:k :player :v "name"} {:k :points :v "points"}]}

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
      (map #(clojure.set/rename-keys %
                                     {:k :dataIndex
                                      :v :title}))))

(defn- transform
  [data tr]
  (reduce-kv update data tr))

(defn- tag
  [t]
  (fn [v] [t (if (float? v) (int v) v)]))

(defn new-table
  [kw]
  [ant/table
   {:columns (to-column-defs kw)
    :dataSource []
    :size "small"
    :pagination false
    :loading false
    :bordered true}])

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


(defn stats-component
  [kw]
  (let [{:keys [handler fields transform title]} (kw stats-config)
        stats (rf/subscribe [handler])
        active-player-names (rf/subscribe [::players-handlers/active-players-names])]

    (s/assert kw @stats)
    [:div
     [:label title]
     [stats-table
      fields
      (take stats-length
            (filter #(@active-player-names (:player %)) @stats))

      (or transform {})]]))
