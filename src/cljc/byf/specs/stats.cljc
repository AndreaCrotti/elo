(ns byf.specs.stats
  (:require [clojure.spec.alpha :as s]))

(defn percent?
  [n]
  (and (>= n 0)
       (<= n 100)))

(s/def ::player string?)
(s/def ::ranking float?)
(s/def ::game-idx int?)
(s/def ::result string?)

(s/def ::streak int?)
(s/def ::points float?)

(s/def ::w percent?)
(s/def ::d percent?)
(s/def ::l percent?)

(s/def ::highest-ranking
  (s/coll-of (s/keys :req-un [::player
                              ::ranking
                              ::game-idx
                              ::time
                              ::result])))
(s/def ::longest-streak
  (s/coll-of (s/keys :req-un [::player
                              ::streak])))

(s/def ::highest-increase
  (s/coll-of (s/keys :req-un [::player
                              ::points])))

(s/def ::best-percents
  (s/coll-of (s/keys :req-un [::player
                              ::w
                              ::d
                              ::l])))
