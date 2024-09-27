(ns data-migrations.data-type-migration
  (:require [byf.db :as db]
            [honeysql.core :as sql]
            [honeysql.helpers :as h]
            [clojure.java.jdbc :as jdbc]
            [byf.generators :as gen]))

;; first create all the games supported and then get all the leagues
;; and set the game_kind field accordingly

(def kinds
  [{:name         "fifa"
    :draw-allowed true
    :min-points   0
    :max-points   10}

   {:name "pool"
    :draw-allowed false
    :min-points 0
    :max-points 2}

   {:name "table-tennis"
    :draw-allowed false
    :min-points 0
    :max-points 4}])
