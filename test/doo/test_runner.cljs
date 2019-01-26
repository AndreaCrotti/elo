(ns doo.test-runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [elo.rankings-test]
              [elo.games-test]
              [elo.stats-test]
              [elo.algorithms.elo-test]))

(doo-tests 'elo.rankings-test
           'elo.games-test
           'elo.stats-test
           'elo.algorithms.elo-test)
