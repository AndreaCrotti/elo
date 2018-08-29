(ns elo.generators
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]))

(def is-uuid? uuid?)

(s/def ::id is-uuid?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::player (s/keys :req-un [::id
                                 ::name
                                 ::email]))

(s/def ::p1 is-uuid?)
(s/def ::p2 is-uuid?)
(s/def ::league_id is-uuid?)

(s/def ::p1_team string?)
(s/def ::p2_team string?)

(s/def ::p1_goals (s/int-in 0 10))
(s/def ::p2_goals (s/int-in 0 10))

(s/def ::game (s/keys :req-un [::id
                               ::p1
                               ::p2
                               ::p1_team
                               ::p2_team
                               ::p1_goals
                               ::p2_goals
                               ::league_id]))

(s/def ::name string?)

(s/def ::league (s/keys :req-un [::id
                                 ::name]))

(defn gen
  [spec]
  (fn ([ks]
      (merge ks (g/generate (s/gen spec))))))

(def game-gen (gen ::game))

(def player-gen (gen ::player))

(def league-gen (gen ::league))

;; TODO: use this to test that the whole sum of many games is a multiple of 1500
(s/def ::normalized-game (s/coll-of keyword? keyword? (s/int-in 0 1)))
