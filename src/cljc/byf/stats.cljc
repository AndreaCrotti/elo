(ns byf.stats
  (:require [medley.core :as medley]
            [byf.games :as games]))

(defn uuid->name
  [name-mapping vals]
  (medley/map-keys #(get name-mapping %) vals))

(defn longest-by
  [longest-fn]
  (fn [results name-mapping]
    (->> results
         (medley/map-vals longest-fn)
         (uuid->name name-mapping)
         (sort-by #(- (second %)))
         (map #(zipmap [:player :streak] %)))))

(def longest-streak (longest-by games/longest-winning-subseq))

(def longest-unbeaten (longest-by games/longest-unbeaten-subseq))

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

(defn- extract-percents
  [results]
  (let [freq (frequencies results)
        cent-fn #(if (contains? freq %)
                   (* 100 (/ (% freq) (count results)))
                   0)]

    (map cent-fn [:w :d :l])))

(defn best-percents
  [results name-mapping]
  (->> results
       (medley/map-vals extract-percents)
       (uuid->name name-mapping)
       (into [])
       (sort-by (comp first second))
       (map flatten)
       (map #(zipmap [:player :w :d :l] %))
       (reverse)))
