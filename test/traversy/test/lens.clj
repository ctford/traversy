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

(fact "We can combine single-focus lenses."
  (-> {:foo {:bar 9}} (view (combine (in [:foo]) (in [:bar])))) => 9
  (-> {:foo {:bar 9}} (update (combine (in [:foo]) (in [:bar])) inc)) => {:foo {:bar 10}})

(fact "We can combine multiple then single-focus lenses."
  (-> [{:foo 1} {:foo 2}] (view (combine each (in [:foo])))) => [1 2]
  (-> [{:foo 1} {:foo 2}] (update (combine each (in [:foo])) inc)) => [{:foo 2} {:foo 3}])
