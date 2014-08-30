(ns traversy.test.lens
  (:require [midje.sweet :refer :all]
            [traversy.lens :refer :all]))

(fact "The 'it' lens is the identity."
  (-> 9 (view it)) => 9
  (-> 9 (view-all it)) => [9]
  (-> 9 (update it inc)) => 10)

(fact "The 'nothing' lens doesn't have a focus."
  (-> 9 (view-all nothing)) => []
  (-> 9 (update nothing inc)) => 9)

(fact "Trying to 'view' a lens with more than one focus throws an error."
  (-> [9 10] (view each)) => (throws AssertionError))

(fact "Using 'view' with a multi-focus lens that happens to only have a single focus is fine."
  (-> [9 10] (view (only even?))) => 10)

(fact "The 'in' lens focuses into a map based on a path."
  (-> {:foo 1} (view (in [:foo]))) => 1
  (-> {:foo 1} (view-all (in [:foo]))) => [1]
  (-> {:foo 1} (update (in [:foo]) inc)) => {:foo 2})

(fact "The 'each' lens focuses on each item in a sequence."
  (-> [1 2 3] (view-all each)) => [1 2 3]
  (-> [1 2 3] (update each inc)) => #(and (= % [2 3 4]) vector?)
  (-> [1 2 3] seq (update each inc)) => #(and (= % [2 3 4]) seq?))

(fact "The 'each' lens focuses on each element in a set."
  (-> #{1 2 3} (view-all each)) => (just #{1 2 3})
  (-> #{1 2 3} (update each inc)) => #{2 3 4})

(fact "The 'each' lens focuses on the entries of a map."
  (-> {:foo 3 :bar 4} (view-all all-entries)) => (just #{[:foo 3] [:bar 4]})
  (-> {:foo 3 :bar 4} (update all-entries (fn [[k v]] [v k]))) => {3 :foo 4 :bar})

(fact "The 'all-entries' lens focuses on the entries of a map."
  (-> {:foo 3 :bar 4} (view-all all-entries)) => (just #{[:foo 3] [:bar 4]})
  (-> {:foo 3 :bar 4} (update all-entries (fn [[k v]] [v k]))) => {3 :foo 4 :bar})

(fact "The 'all-values' lens focuses on the values of a map."
  (-> {:foo 1 :bar 2} (view-all all-values)) => (just #{1 2})
  (-> {:foo 1 :bar 2} (update all-values inc)) => {:foo 2 :bar 3})

(fact "The 'all-keys' lens focuses on the keys of a map."
  (-> {:foo 1 :bar 2} (view-all all-keys)) => (just #{:foo :bar})
  (-> {:foo 1 :bar 2} (update all-keys {:foo :frag :bar :barp})) => {:frag 1 :barp 2})

(fact "The 'maybe' lens focuses only on foci that are present."
  (-> {:foo 1} (view-all (*> (in [:foo]) maybe))) => [1]
  (-> {:foo 1} (view-all (*> (in [:bar]) maybe))) => []
  (-> {:foo 1} (view-all (*> (+> (in [:foo]) (in [:bar])) maybe)))) => [1]

(fact "The 'only' lens focuses on the items in a sequence matching a condition."
  (-> [1 2 3] (view-all (only even?))) => [2]
  (-> [1 2 3] (update (only even?) inc)) => [1 3 3]
  (-> #{1 2 3} (update (only even?) inc)) => #{1 3})

(fact "The 'select-entries' lens focuses on entries of a map specified by key."
  (-> {:foo 3 :bar 4 :baz 5} (view-all (select-entries [:foo :bar]))) => (just #{[:foo 3] [:bar 4]})
  (-> {:foo 3 :bar 4 :baz 5} (update (select-entries [:foo :bar]) (fn [[k v]] [v k]))) => {3 :foo 4 :bar :baz 5})

(fact "The entries lenses support deletion."
  (-> {:foo 3 :bar 4 :baz 5} (update (select-entries [:foo :bar]) delete)) => {:baz 5}
  (-> {:foo 3 :bar 4 :baz 5} (update all-entries delete)) => {})

(fact "put sets the value at all the foci of a lens."
  (-> [1 2 3] (update (only even?) (put 7))) => [1 7 3]
  (-> #{1 2 3} (update each (put 7))) => #{7}
  (-> {:foo 3 :bar 4} (update (select-entries [:foo]) (put [:baz 7]) )) => {:bar 4 :baz 7})

(fact "The items lenses support deletion."
  (-> [1 2 3] (update each delete)) => [])

(fact "The 'only' lens supports deletion."
  (-> [1 2 3] (update (only even?) delete)) => [1 3]
  (-> [1 2 3] (update (only (complement even?)) delete)) => [2]
  (-> [1 2 3] (update (only (complement even?)) delete)) => vector?)

(fact "The 'each' lens supports deletion on sets."
  (-> #{1 2 3} (update each delete)) => #{})

(fact "The 'xth' lens focuses on the nth item of a sequence."
  (-> [2 3 4] (view (xth 1))) => 3
  (-> [2 3 4] (view-all (xth 1))) => [3]
  (-> [2 3 4] (update (xth 1) inc)) => [2 4 4])

(fact "We can 'combine' single-focus lenses."
  (-> {:foo {:bar 9}} (view (combine (in [:foo]) (in [:bar])))) => 9
  (-> {:foo {:bar 9}} (view-all (combine (in [:foo]) (in [:bar])))) => [9]
  (-> {:foo {:bar 9}} (update (combine (in [:foo]) (in [:bar])) inc)) => {:foo {:bar 10}})

(fact "We can 'combine' multiple-focus lenses with single-focus lenses."
  (-> [{:foo 1} {:foo 2}] (view-all (combine each (in [:foo])))) => [1 2]
  (-> [{:foo 1} {:foo 2}] (update (combine each (in [:foo])) inc)) => [{:foo 2} {:foo 3}])

(fact "We can 'combine' multiple-focus lenses with multiple-focus lenses."
  (-> [[1 2] [3]] (view-all (combine each each))) => [1 2 3]
  (-> [[1 2] [3]] (update (combine each each) inc)) => [[2 3] [4]])

(fact "We can combine single-focus lenses with multiple-focus lenses."
  (-> {:foo [1 2]} (view-all (combine (in [:foo]) each))) => [1 2]
  (-> {:foo [1 2]} (update (combine (in [:foo]) each) inc)) => {:foo [2 3]})

(fact "We can combine n lenses with '*>'."
  (-> {:foo {:bar {:baz 9}}} (view (*> (in [:foo]) (in [:bar]) (in [:baz])))) => 9
  (-> {:foo {:bar {:baz 9}}} (view-all (*> (in [:foo]) (in [:bar]) (in [:baz])))) => [9]
  (-> {:foo {:bar {:baz 9}}} (update (*> (in [:foo]) (in [:bar]) (in [:baz])) inc)) => {:foo {:bar {:baz 10}}})

(fact "We can combine lenses in parallel with 'both'."
  (-> {:foo 8 :bar 9} (view-all (both (in [:foo]) (in [:bar])))) => [8 9]
  (-> {:foo 8 :bar 9} (update (both (in [:foo]) (in [:bar])) inc)) => {:foo 9 :bar 10})

(fact "We can combine lenses in parallel with '+>'."
  (-> {:foo 8 :bar 9 :baz 10} (view-all (+> (in [:foo]) (in [:bar]) (in [:baz])))) => [8 9 10]
  (-> {:foo 8 :bar 9 :baz 10} (update (+> (in [:foo]) (in [:bar]) (in [:baz])) inc)) => {:foo 9 :bar 10 :baz 11})
