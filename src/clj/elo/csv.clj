(ns elo.csv
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(defn write-csv
  "Return a function closed over header rows that take an output stream
  and write out the CSV concatenating headers and rows together"
  [header rows]
  (fn [out]
    (with-open [wtr (io/writer out)]
      (csv/write-csv wtr
                     (cons (map name header) rows)))))
