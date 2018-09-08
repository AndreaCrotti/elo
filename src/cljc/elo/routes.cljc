(ns elo.routes)

(def routes
  ["/" {["league/" :league-id] :league-detail
        "leagues/" :league-list
        "admin/" :admin}])
