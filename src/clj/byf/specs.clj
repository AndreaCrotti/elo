(ns byf.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::p1_name string?)
(s/def ::p2_name string?)
(s/def ::p1_using string?)
(s/def ::p2_using string?)
(s/def ::p1_points int?)
(s/def ::p2_points int?)

(s/def ::game (s/keys :req-un [::p1_name
                               ::p2_name
                               ::p1_using
                               ::p2_using
                               ::p1_points
                               ::p2_points]))
