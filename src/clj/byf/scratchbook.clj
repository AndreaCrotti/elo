(ns byf.scratchbook
  (:require [clojure.java.jdbc :as jdbc]
            [honeysql.core :as sql]))

(def prod-db-url "postgres://bcdcrxuwwdownt:0d184283d6d23af0d3fb545c260ba3135164f08e1ca13e5b491e95b0dbe9103c@ec2-54-247-123-231.eu-west-1.compute.amazonaws.com:5432/ddtbj3dr5jd9vf")

(comment
  (with-redefs [db-spec (constantly prod-db-url)]
    (first (map (juxt :id :game_type) (load-leagues)))));; => [#uuid "f062cc50-d5a4-4494-b53c-73ff87992349" "fifa"]

(comment
  (with-redefs [db-spec (constantly prod-db-url)]
    (add-player-full!
     {:name "Razvan"
      :email "razvan.spatariu@fundingcircle.com"
      :league_id #uuid "f062cc50-d5a4-4494-b53c-73ff87992349"})))
