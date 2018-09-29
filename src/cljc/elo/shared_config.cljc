(ns elo.shared-config)

(def timestamp-format "YYYY-MM-DDZHH:mm:SS")

(def games #{"fifa" "street-fighter" "table-tennis"})

;;TODO: add a spec for the game configuration
(def games-config
  {:fifa
   {:terminology {:using "team"
                  :points "goals"}
    :form {:points (range 10)}
    :logo "fifa.png"
    :draw? true}

   :street-fighter
   {:terminology {:using "character"
                  :points "rounds"}
    :form {:points (range 3)}
    :logo "street_fighter.gif"
    :draw? false}

   :table-tennis
   {:terminology {:using "player"
                  :points "games"}
    :form {:points (range 6)}
    :logo "table_tennis.png"
    :draw? false}})

(defn term
  [game k]
  (-> games-config game :terminology k))

(defn opts
  [game k]
  (-> games-config game :form k))

(defn logo
  [game]
  (str "/logos/" (-> games-config game :logo)))
