(ns elo.core)

(def k 32)

(defn expected
  [diff]
  (/ 1 (+ 1 (Math/pow 10 (/ diff 400)))))

(defn new-rating
  [old expected score]
  (+ old (* k (- score expected))))

(def initial-ratings
  {:a 1500
   :b 1500
   :c 1500})

(def games
  [[:a :b 1]
   [:b :c 0.5]
   [:a :c (- 1)]])

(defn new-ratings
  [ratings [p1 p2 score]]

  (let [ra (p1 ratings)
        rb (p2 ratings)]

    (assoc ratings
           p1 (new-rating ra
                          (expected (- rb ra))
                          score)

           p2 (new-rating rb
                          (expected (- ra rb))
                          (- score)))))

(defn update-ratings
  [ratings scores]
  (if (empty? scores)
    ratings
    (update-ratings (new-ratings ratings
                                 (first scores))
                    (rest scores))))
