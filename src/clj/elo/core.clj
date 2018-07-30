(ns elo.core)

(def k 32)

(defn expected
  [diff]
  (/ 1 (inc (Math/pow 10 (/ diff 400)))))

(defn new-rating
  [old expected game]
  (+ old (* k (- game expected))))

(defn new-ratings
  [ratings [p1 p2 game]]

  (let [ra (p1 ratings)
        rb (p2 ratings)]

    (assoc ratings
           p1 (new-rating ra
                          (expected (- rb ra))
                          game)

           p2 (new-rating rb
                          (expected (- ra rb))
                          (- game)))))

(defn update-ratings
  [ratings games]
  (if (empty? games)
    ratings
    (recur (new-ratings ratings (first games))
           (rest games))))
