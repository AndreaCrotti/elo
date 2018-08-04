(ns elo.helpers)

(def ^:dynamic *test-db*)
(def test-db-uri "postgres://elo@localhost:5445/elo_test")

(defn wrap-db-call
  [test-fn]
  (binding [*test-db* test-db-uri]
    (test-fn)))
