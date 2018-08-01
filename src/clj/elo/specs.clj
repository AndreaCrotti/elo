(ns elo.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::p1-name string?)
(s/def ::p2-name string?)
(s/def ::p1-team string?)
(s/def ::p2-team string?)
(s/def ::p1-goals int?)
(s/def ::p2-goals int?)

(s/def ::game (s/keys :req-un [::p1-name
                               ::p2-name
                               ::p1-team
                               ::p2-team
                               ::p1-goals
                               ::p2-goals]))
