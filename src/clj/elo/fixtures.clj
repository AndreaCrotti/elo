(ns elo.fixtures
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]))

(s/def ::id uuid?)
(s/def ::p1_goals (s/int-in 0 10))
(s/def ::p2_goals (s/int-in 0 10))

(s/def ::team-name string?)

(s/def ::player-name string?)

(s/def ::player-email string?)
