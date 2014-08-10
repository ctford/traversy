(ns traversy.test.lens
  (:require [midje.sweet :refer :all]
            [traversy.lens :refer :all]))

(fact "The identity lens is non-prescription."
  (-> 9 (view it)) => 9
  (-> 9 (update it inc)) => 10)

(fact "The each lens focuses on each item in a sequence."
  (-> [1 2 3] (view each)) => [1 2 3]
  (-> [1 2 3] (update each inc)) => [2 3 4])

(fact "The in lens focuses into a map based on a path."
  (-> {:foo 1} (view (in [:foo]))) => 1
  (-> {:foo 1} (update (in [:foo]) inc)) => {:foo 2})

(fact "The elements lens focuses on each element in a set."
  (-> #{1 2 3} (view elements)) => (just #{1 2 3})
  (-> #{1 2 3} (update elements inc)) => #{2 3 4})
