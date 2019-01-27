(ns elo.vega
  (:require [clojure.set :as set]))

(defn history-vega
  [history]
  (let [kw->keyname {:player "Player"
                     :ranking "Ranking"
                     :game-idx "Game #"
                     :time "Time"}]

    (->> history
         (map #(update % :game-idx inc))
         (map #(set/rename-keys % kw->keyname)))))
