(ns traversy.test.lens
  (:require [midje.sweet :refer :all]
            [traversy.lens :refer :all]))

(fact "The identity lens is non-prescription."
  (-> 9 (view id)) => 9
  (-> 9 (update id inc)) => 10)

(fact "The each lens focuses on each item in a sequence."
  (-> [1 2 3] (view each)) => [1 2 3]
  (-> [1 2 3] (update each inc)) => [2 3 4])
