(ns elo.games)

(defmulti normalize-game :game)

(defmethod normalize-game :fifa
  [{:keys [p1 p2 p1_goals p2_goals]}]
  (cond
    (= p1_goals p2_goals) [p1 p2 0.5 0.5]
    (> p1_goals p2_goals) [p1 p2 1 0]
    (> p2_goals p1_goals) [p1 p2 0 1]))
