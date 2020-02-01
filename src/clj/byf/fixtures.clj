(ns byf.fixtures
  (:require [clojure.spec.alpha :as s]))

(s/def ::id uuid?)
(s/def ::name string?)
(s/def ::email string?)
(s/def ::player (s/keys :req-un [::id
                                 ::name
                                 ::email]))

(s/def ::p1 uuid?)
(s/def ::p2 uuid?)
(s/def ::p1_using string?)
(s/def ::p2_using string?)
(s/def ::p1_points (s/int-in 0 10))
(s/def ::p2_points (s/int-in 0 10))

;;TODO: add recorded and played_at

(s/def ::game (s/keys :req-un [::id
                               ::p1
                               ::p2
                               ::p1_using
                               ::p2_using
                               ::p1_points
                               ::p2_points]))