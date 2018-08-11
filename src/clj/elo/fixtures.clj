(ns elo.fixtures
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]))

(s/def ::id uuid?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::player (s/keys :req-un [::id
                                 ::name
                                 ::email]))

(s/def ::p1 uuid?)
(s/def ::p2 uuid?)
(s/def ::p1_team string?)
(s/def ::p2_team string?)
(s/def ::p1_goals (s/int-in 0 10))
(s/def ::p2_goals (s/int-in 0 10))

;;TODO: add recorded and played_at

(s/def ::game (s/keys :req-un [::id
                               ::p1
                               ::p2
                               ::p1_team
                               ::p2_team
                               ::p1_goals
                               ::p2_goals]))

(defn game-gen
  []
  (s/gen ::game))

(defn player-gen
  []
  (s/gen ::player))
