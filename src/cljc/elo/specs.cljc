(ns elo.specs
  (:require [clojure.spec.alpha :as s]))

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
