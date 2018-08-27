(ns elo.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [elo.algorithms.elo]
            [elo.algorithms.elo-test]))

(doo-tests 'elo.algorithms.elo-test)
