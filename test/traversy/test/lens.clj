(ns traversy.test.lens
  (:require [midje.sweet :refer :all]
            [traversy.lens :refer :all]))

(fact "The 'it' lens is the identity."
  (-> 9 (view it)) => 9
  (-> 9 (collect it)) => [9]
  (-> 9 (update it inc)) => 10)

(fact "The 'each' lens focuses on each item in a sequence."
  (-> [1 2 3] (collect each)) => [1 2 3]
  (-> [1 2 3] (update each inc)) => [2 3 4])

(fact "The 'in' lens focuses into a map based on a path."
  (-> {:foo 1} (view (in [:foo]))) => 1
  (-> {:foo 1} (collect (in [:foo]))) => [1]
  (-> {:foo 1} (update (in [:foo]) inc)) => {:foo 2})

(fact "The 'elements' lens focuses on each element in a set."
  (-> #{1 2 3} (collect elements)) => (just #{1 2 3})
  (-> #{1 2 3} (update elements inc)) => #{2 3 4})

(fact "The 'all-values' lens focuses on the values of a map."
  (-> {:foo 1 :bar 2} (collect all-values)) => (just #{1 2})
  (-> {:foo 1 :bar 2} (update all-values inc)) => {:foo 2 :bar 3})

(fact "The 'all-keys' lens focuses on the keys of a map."
  (-> {:foo 1 :bar 2} (collect all-keys)) => (just #{:foo :bar})
  (-> {:foo 1 :bar 2} (update all-keys {:foo :frag :bar :barp})) => {:frag 1 :barp 2})

(fact "The 'only' lens focuses on the items in a sequence matching a condition."
  (-> [1 2 3] (collect (only even?))) => [2]
  (-> [1 2 3] (update (only even?) inc)) => [1 3 3])

(fact "The 'entries' lens focuses on the entries of a map."
  (-> {:foo 3 :bar 4} (collect all-entries)) => (just #{[:foo 3] [:bar 4]})
  (-> {:foo 3 :bar 4} (update all-entries (fn [[k v]] [v k]))) => {3 :foo 4 :bar})

(fact "The 'xth' lens focuses on the nth item of a sequence."
  (-> [2 3 4] (view (xth 1))) => 3
  (-> [2 3 4] (collect (xth 1))) => [3]
  (-> [2 3 4] (update (xth 1) inc)) => [2 4 4])

(fact "We can 'combine' single-focus lenses."
  (-> {:foo {:bar 9}} (view (combine (in [:foo]) (in [:bar])))) => 9
  (-> {:foo {:bar 9}} (collect (combine (in [:foo]) (in [:bar])))) => [9]
  (-> {:foo {:bar 9}} (update (combine (in [:foo]) (in [:bar])) inc)) => {:foo {:bar 10}})

(fact "We can 'combine' multiple-focus lenses with single-focus lenses."
  (-> [{:foo 1} {:foo 2}] (collect (combine each (in [:foo])))) => [1 2]
  (-> [{:foo 1} {:foo 2}] (update (combine each (in [:foo])) inc)) => [{:foo 2} {:foo 3}])

(fact "We can 'combine' multiple-focus lenses with multiple-focus lenses."
  (-> [[1 2] [3]] (collect (combine eachv eachv))) => [1 2 3]
  (-> [[1 2] [3]] (update (combine eachv eachv) inc)) => [[2 3] [4]])

(fact "We can combine single-focus lenses with multiple-focus lenses."
  (-> {:foo [1 2]} (collect (combine (in [:foo]) eachv))) => [1 2]
  (-> {:foo [1 2]} (update (combine (in [:foo]) eachv) inc)) => {:foo [2 3]})

(fact "We can combine n lenses with '+>'."
  (-> {:foo {:bar {:baz 9}}} (view (+> (in [:foo]) (in [:bar]) (in [:baz])))) => 9
  (-> {:foo {:bar {:baz 9}}} (collect (+> (in [:foo]) (in [:bar]) (in [:baz])))) => [9]
  (-> {:foo {:bar {:baz 9}}} (update (+> (in [:foo]) (in [:bar]) (in [:baz])) inc)) => {:foo {:bar {:baz 10}}})
