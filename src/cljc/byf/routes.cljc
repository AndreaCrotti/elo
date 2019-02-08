(ns byf.routes
  (:require [bidi.bidi :as bidi]))

(def routes
  ["/" {["league/" :league-id] :league-detail
        ;;TODO: could redirect to "/leagues" instead ideally here
        "" :league-list
        "admin" :admin
        ["user/" :player-id] :player-detail}])

(def path-for (partial bidi/path-for routes))

(def match-route (partial bidi/match-route routes))
