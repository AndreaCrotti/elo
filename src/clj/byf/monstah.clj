(ns byf.monstah
  (:require [reifyhealth.specmonstah.core :as sm]
            [byf.generators :as gen]
            [byf.db :as db]
            [loom.attr :as lat]
            [reifyhealth.specmonstah.spec-gen :as sg]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [loom.io :as lio]))


(def schema
  {:player {:prefix :p
            :spec ::gen/player
            :relations {:user_id [:user :id]}}

   :game {:prefix :g
          :spec ::gen/game
          :relations {:p1 [:player :id]
                      :p2 [:player :id]}}

   :user {:prefix :u
          :spec ::gen/user}})

(defn make-db
  []
  (-> (sg/ent-db-spec-gen {:schema schema}
                          {:game [[1]]
                           :player [[2]]})

      (sm/attr-map :spec-gen)))

(comment
  (make-db))

(def id-seq (atom 0))
(def ent-db (atom []))

(defn insert*
  [{:keys [data] :as db} ent-name ent-attr-key]
  (swap! ent-db conj [(lat/attr data ent-name :ent-type)
                      (lat/attr data ent-name sg/spec-gen-ent-attr-key)]))

(defn insert [query]
  (reset! id-seq 0)
  (reset! ent-db [])
  (-> (sg/ent-db-spec-gen {:schema schema} query)
      (sm/visit-ents-once :inserted-data insert*)))

(comment
  (insert {:game [[2]]}))
