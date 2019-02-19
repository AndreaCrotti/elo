(ns byf.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [byf.algorithms.elo-test]
            [byf.rankings-test]
            [byf.stats-test]
            [byf.games-test]))

(doo-tests 'byf.algorithms.elo-test
           'byf.rankings-test
           'byf.stats-test
           'byf.games-test)
