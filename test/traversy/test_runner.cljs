(ns traversy.test-runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [traversy.test.lens]))

(doo-all-tests #"traversy\.test\..*")
