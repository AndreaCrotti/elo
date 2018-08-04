(ns elo.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::p1_name string?)
(s/def ::p2_name string?)
(s/def ::p1_team string?)
(s/def ::p2_team string?)
(s/def ::p1_goals int?)
(s/def ::p2_goals int?)

(s/def ::game (s/keys :req-un [::p1_name
                               ::p2_name
                               ::p1_team
                               ::p2_team
                               ::p1_goals
                               ::p2_goals]))
