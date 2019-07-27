(ns byf.generators
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.generators]
            [clojure.spec.gen.alpha :as g]))

(def is-uuid? uuid?)

(s/def ::id is-uuid?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::user_id uuid?)
(s/def ::active boolean?)

(s/def ::player (s/keys :req-un [::id
                                 ::email
                                 ::active
                                 ::name
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
