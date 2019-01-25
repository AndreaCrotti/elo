(ns elo.stats
  (:require [medley.core :as medley]
            [elo.games :as games]))

(defn uuid->name
  [name-mapping vals]
  (medley/map-keys #(get name-mapping %) vals))

(defn longest-streak
  [results name-mapping]
  (->> results
       (medley/map-vals games/longest-winning-subseq)
       (uuid->name name-mapping)
       (sort-by #(- (second %)))
       (map #(zipmap [:player :streak] %))))

(defn highest-rankings-best
  [history]
  (map second
       (sort-by
        (fn [[_ v]]
          (- (:ranking v)))

        (medley/map-vals
         (fn [vs] (last
                  (sort-by :ranking vs)))

         (group-by :player history)))))

(defn highest-increase
  [history]
  (->> history
       (group-by :player)
       (medley/map-vals #(map :ranking %))
       (medley/map-vals games/highest-increase-subseq)
       (sort-by #(- (second %)))
       (map #(zipmap [:player :points] %))))
