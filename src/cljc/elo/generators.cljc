(ns elo.generators
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]))

(def is-uuid? uuid?)

(s/def ::id is-uuid?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::user_id uuid?)

(s/def ::player (s/keys :req-un [::id
                                 ::name
                                 ::email
                                 ::user_id]))

(s/def ::p1 is-uuid?)
(s/def ::p2 is-uuid?)
(s/def ::league_id is-uuid?)

(s/def ::p1_using string?)
(s/def ::p2_using string?)

(s/def ::p1_points (s/int-in 0 10))
(s/def ::p2_points (s/int-in 0 10))

(s/def ::game (s/keys :req-un [::id
                               ::p1
                               ::p2
                               ::p1_using
                               ::p2_using
                               ::p1_points
                               ::p2_points
                               ::league_id]))

(s/def ::name string?)

(s/def ::league (s/keys :req-un [::id
                                 ::name]))

(defn- gen-single
  ([spec]
   (gen-single spec {}))

  ([spec ks]
   (merge (g/generate (s/gen spec)) ks)))

(defn gen
  [spec]
  (partial gen-single spec))

(def game-gen (gen ::game))

(def player-gen (gen ::player))

(def league-gen (gen ::league))

;; TODO: use this to test that the whole sum of many games is a multiple of 1500
(s/def ::normalized-game (s/coll-of keyword? keyword? (s/int-in 0 1)))

;; can make it more specialized?
(s/def ::oauth2_token string?)

(s/def ::user (s/keys :req-un [::id
                               ::oauth2_token
                               ::email]))

(def user-gen (gen ::user))
