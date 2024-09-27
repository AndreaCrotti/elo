(ns byf.shared-config)

(def timestamp-format "YYYY-MM-DDZHH:mm:SS")

(def games #{"fifa"
             "street-fighter"
             "table-tennis"
             "pool"})

;;TODO: add a spec for the game configuration
(def games-config
  {:fifa
   {:terminology   {:using  "team"
                    :points "goals"}
    :team-required true
    :form          {:points (range 10)}
    :logo          "fifa.png"
    :draw?         true}

   :street-fighter
   {:terminology   {:using  "character"
                    :points "rounds"}
    :form          {:points (range 3)}
    :team-required true
    :logo          "street_fighter.gif"
    :draw?         false}
   
   :table-tennis
   {:terminology   {:using  "player"
                    :points "games"}
    :team-required false
    :form          {:points (range 6)}
    :logo          "table_tennis.png"
    :draw?         false}

   :pool
   {:terminology   {:using  "player"
                    :points "games"}
    :team-required false
    :form          {:points (range 3)}
    :logo          "pool.jpg"
    :draw?         false}})

(defn term
  [game k]
  (-> games-config game :terminology k))

(defn opts
  [game k]
  (-> games-config game :form k))

(defn logo
  [game]
  (str "/logos/" (-> games-config game :logo)))

(def default-game-config
  { ;; valid range from 20 to 44
   :k 32
   ;; valid range from 100 to 2000
   ;; should also move the k accordingly
   :initial-ranking 1500
   ;; should be a percent from 0 to 1
   :points-count 0})
