(ns elo.shared-config)

(def timestamp-format "YYYY-MM-DDZHH:mm:SS")

(def games #{"fifa" "street-fighter" "table-tennis"})

(def games-config
  {:fifa
   {:terminology {:using "team"
                  :points "goals"}
    :form {:points (range 10)}
    :logo "fifa.png"}

   :street-fighter
   {:terminology {:using "character"
                  :points "rounds"}
    :form {:points (range 3)}
    :logo "street_fighter.gif"}})

(defn term
  [game k]
  (-> games-config game :terminology k))

(defn opts
  [game k]
  (-> games-config game :form k))

(defn logo
  [game]
  (str "logos/" (-> games-config game :logo)))
