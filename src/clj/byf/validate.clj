(ns byf.validate
  "Validate and conform data received from the API"
  (:require [clj-time.coerce :as tc]
            [clj-time.format :as f])
  (:import (java.util UUID)))

(def timestamp-format "YYYY-MM-ddZHH:mm:SS")

(defn to-uuid
  [uuid-str]
  (some-> uuid-str
          (UUID/fromString)))

(defn parse-int
  [int-str]
  (some-> int-str
          (Integer/parseInt)))

(def game-transformations
  {:p1 to-uuid
   :p2 to-uuid
   :league_id to-uuid
   :p1_points parse-int
   :p2_points parse-int
   :played_at #(tc/to-sql-time (f/parse
                                (f/formatter timestamp-format) %))})

(defmulti conform-data
  (fn [type data] type))

(defn apply-transformations
  [data transformations]
  (select-keys
   (reduce-kv update data transformations)
   (keys data)))

(defmethod conform-data :game
  [_ data]
  (apply-transformations data game-transformations))

(defmethod conform-data :player
  [_ data]
  (apply-transformations data {:league_id to-uuid}))
