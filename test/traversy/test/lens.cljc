(ns traversy.test.lens
  (:refer-clojure :exclude [update])
  (:require [traversy.lens :refer [view-single view update it nothing each only in update
                                   indexed all-entries all-values all-keys select-entries
                                   conditionally put xth combine both *> +> maybe]]
    ;; NB cljs doesn't support :refer :all
            #?(:clj [clojure.test :refer [deftest is testing]]
               :cljs [cemerick.cljs.test :as t]))
  #?(:cljs (:require-macros [cemerick.cljs.test :refer [deftest is testing]])))

(deftest test-it
  (testing "The 'it' lens is the identity."
    (is (= (-> 9 (view-single it)) 9))
    (is (= (-> 9 (view it)) [9]))
    (is (= (-> 9 (update it inc)) 10))))

(deftest test-nothing
  (testing "The 'nothing' lens doesn't have a focus."
    (is (= (-> 9 (view nothing)) '()))
    (is (= (-> 9 (update nothing inc)) 9))))

(deftest test-view-single
  (testing "Trying to 'view-single' a lens that doesn't have exactly one focus throws an error."
    (is (thrown? #?(:clj AssertionError :cljs js/Error) (-> [9 10] (view-single each))))
    (is (thrown? #?(:clj AssertionError :cljs js/Error) (-> [] (view-single each)))))

  (testing "Using 'view-single' with a multi-focus lens that happens to only have a single focus is fine."
    (is (= (-> [9 10] (view-single (only even?))) 10))))

(deftest test-in
  (testing "The 'in' lens focuses into a map based on a path."
    (is (= (-> {:foo 1} (view-single (in [:foo]))) 1))
    (is (= (-> {:foo 1} (view (in [:foo]))) [1]))
    (is (= (-> {:foo 1} (update (in [:foo]) inc)) {:foo 2}))
    (is (= (-> {:foo 1} (view-single (in [:bar] "not-found"))) "not-found"))
    (is (= (-> {:foo 1} (view-single (in [:bar]))) nil)))
  (testing "Unlike 'update-in', 'in' does nothing if the specified path does not exist."
    (is (= (-> {} (update (in [:foo]) identity)) {}))))

(deftest test-each
  (testing "The 'each' lens focuses on each item in a sequence."
    (is (= (-> [1 2 3] (view each)) [1 2 3]))
    (is (= (-> [] (view each)) '()))
    (is (#(and (= % [2 3 4]) (vector? %)) (-> [1 2 3] (update each inc))))
    (is (#(and (= % [2 3 4]) seq?) (-> [1 2 3] seq (update each inc)))))

  (testing "The 'each' lens focuses on each element in a set."
    (is (= (-> #{1 2 3} (view each) set) #{1 2 3}))
    (is (= (-> #{} (view each)) '()))
    (is (= (-> #{1 2 3} (update each inc) set) #{2 3 4})))

  (testing "The 'each' lens focuses on the entries of a map."
    (is (= (-> {:foo 3 :bar 4} (view each) set #{[:foo 3] [:bar 4]})))
    (is (= (-> {} (view each)) '()))
    (is (-> {:foo 3 :bar 4} (update each (fn [[k v]] [v k]))) {3 :foo 4 :bar})))

(deftest test-indexed
  (testing "The 'indexed' lens focuses on indexed pairs in a sequence."
    (is (= (-> [1 2 3] (view indexed)) [[0 1] [1 2] [2 3]]))
    (is (= (-> [1 2 3] (update indexed (fn [[i v]] [i (+ i v)]))) [1 3 5]))))

(deftest test-all-entries
  (testing "The 'all-entries' lens focuses on the entries of a map."
    (is (= (-> {:foo 3 :bar 4} (view all-entries) set) #{[:foo 3] [:bar 4]}))
    (is (= (-> {} (view all-entries)) '()))
    (is (= (-> {:foo 3 :bar 4} (update all-entries (fn [[k v]] [v k]))) {3 :foo 4 :bar}))))

(deftest test-all-values
  (testing "The 'all-values' lens focuses on the values of a map."
    (is (= (-> {:foo 1 :bar 2} (view all-values) set) #{1 2}))
    (is (= (-> {:foo 1 :bar 2} (update all-values inc)) {:foo 2 :bar 3}))))

(deftest test-all-keys
  (testing "The 'all-keys' lens focuses on the keys of a map."
    (is (= (-> {:foo 1 :bar 2} (view all-keys) set) #{:foo :bar}))
    (is (= (-> {:foo 1 :bar 2} (update all-keys {:foo :frag :bar :barp})) {:frag 1 :barp 2}))))

(deftest test-conditionally
  (testing "The 'conditionally' lens focuses only on foci that match a condition."
    (is (= (-> 1 (view (conditionally odd?))) [1]))
    (is (= (-> 1 (view (conditionally even?))) '()))
    (is (= (-> {:foo 1 :bar 2} (view (*> (+> (in [:foo]) (in [:bar])) (conditionally odd?)))) [1]))
    (is (= (-> 1 (update (conditionally odd?) inc)) 2))
    (is (= (-> 1 (update (conditionally even?) inc)) 1))))

(deftest test-maybe
  (testing "The 'maybe' lens focuses only on foci that are present."
    (is (= (-> {:foo 1} (view (*> (in [:foo]) maybe))) [1]))
    (is (= (-> {:foo 1} (view (*> (in [:bar]) maybe))) '()))
    (is (= (-> {:foo 1} (view (*> (+> (in [:foo]) (in [:bar])) maybe))) [1]))
    (is (= (-> 1 (update maybe inc)) 2))
    (is (nil? (-> nil (update maybe inc))))))

(deftest test-only
  (testing "The 'only' lens focuses on the items in a sequence matching a condition."
    (is (= (-> [1 2 3] (view (only even?))) [2]))
    (is (= (-> [1 2 3] (update (only even?) inc)) [1 3 3]))
    (is (= (-> #{1 2 3} (update (only even?) inc)) #{1 3}))))

(deftest test-select-entries
  (testing "The 'select-entries' lens focuses on entries of a map specified by key."
    (is (= (-> {:foo 3 :bar 4 :baz 5} (view (select-entries [:foo :bar])) set) #{[:foo 3] [:bar 4]}))
    (is (= (-> {:foo 3 :bar 4 :baz 5} (update (select-entries [:foo :bar]) (fn [[k v]] [v k])))
           {3 :foo 4 :bar :baz 5}))))

(deftest test-put
  (testing "put sets the value at all the foci of a lens."
    (is (= (-> [1 2 3] (update (only even?) (put 7))) [1 7 3]))
    (is (= (-> #{1 2 3} (update each (put 7))) #{7}))
    (is (= (-> {:foo 3 :bar 4} (update (select-entries [:foo]) (put [:baz 7]))) {:bar 4 :baz 7}))))

(deftest test-xth
  (testing "The 'xth' lens focuses on the nth item of a sequence."
    (is (= (-> [2 3 4] (view-single (xth 1))) 3))
    (is (= (-> [2 3 4] (view (xth 1))) [3]))
    (is (= (-> [2 3 4] (update (xth 1) inc)) [2 4 4]))
    (is (= (-> [2 3 4] (view-single (xth 4 "not found"))) "not found"))
    (is (= (-> [2 3 4] (view (xth 4 "not found"))) ["not found"]))))

(deftest test-combine
  (testing "We can 'combine' single-focus lenses."
    (is (= (-> {:foo {:bar 9}} (view-single (combine (in [:foo]) (in [:bar])))) 9))
    (is (= (-> {:foo {:bar 9}} (view (combine (in [:foo]) (in [:bar])))) [9]))
    (is (= (-> {:foo {:bar 9}} (update (combine (in [:foo]) (in [:bar])) inc)) {:foo {:bar 10}})))

  (testing "We can 'combine' multiple-focus lenses with single-focus lenses."
    (is (= (-> [{:foo 1} {:foo 2}] (view (combine each (in [:foo])))) [1 2]))
    (is (= (-> [{:foo 1} {:foo 2}] (update (combine each (in [:foo])) inc)) [{:foo 2} {:foo 3}])))

  (testing "We can 'combine' multiple-focus lenses with multiple-focus lenses."
    (is (= (-> [[1 2] [3]] (view (combine each each))) [1 2 3]))
    (is (= (-> [[1 2] [3]] (update (combine each each) inc)) [[2 3] [4]])))

  (testing "We can combine single-focus lenses with multiple-focus lenses."
    (is (= (-> {:foo [1 2]} (view (combine (in [:foo]) each))) [1 2]))
    (is (= (-> {:foo [1 2]} (update (combine (in [:foo]) each) inc)) {:foo [2 3]}))))

(deftest test-*>
  (testing "We can combine n lenses with '*>'."
    (is (= (-> {:foo {:bar {:baz 9}}} (view-single (*> (in [:foo]) (in [:bar]) (in [:baz])))) 9))
    (is (= (-> {:foo {:bar {:baz 9}}} (view (*> (in [:foo]) (in [:bar]) (in [:baz])))) [9]))
    (is (= (-> {:foo {:bar {:baz 9}}} (update (*> (in [:foo]) (in [:bar]) (in [:baz])) inc)) {:foo {:bar {:baz 10}}}))))

(deftest test-both
  (testing "We can combine lenses in parallel with 'both'."
    (is (= (-> {:foo 8 :bar 9} (view (both (in [:foo]) (in [:bar])))) [8 9]))
    (is (= (-> {:foo 8 :bar 9} (update (both (in [:foo]) (in [:bar])) inc)) {:foo 9 :bar 10}))))

(deftest test-+>
  (testing "We can combine lenses in parallel with '+>'."
    (is (= (-> {:foo 8 :bar 9 :baz 10} (view (+> (in [:foo]) (in [:bar]) (in [:baz])))) [8 9 10]))
    (is (= (-> {:foo 8 :bar 9 :baz 10} (update (+> (in [:foo]) (in [:bar]) (in [:baz])) inc)) {:foo 9 :bar 10 :baz 11}))))
