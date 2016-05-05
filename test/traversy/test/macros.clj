(ns traversy.test.macros
  (:require [clojure.test :refer [deftest is testing are]]
            [traversy.macros :as m]
            [traversy.lens :as l]))

(deftest test-lens-paths
  (testing "can break map into paths"
    (is
      (=
        (m/get-paths {:a 1 :b [{:c 2 :d 3} 4]} integer?)
        [[[:key :a] [:val 1]]
         [[:key :b] [:index 0] [:key :c] [:val 2]]
         [[:key :b] [:index 0] [:key :d] [:val 3]]
         [[:key :b] [:index 1] [:val 4]]]))))

(deftest test-combining-paths
  (testing "combining paths"
    (is (=
          (m/combine-paths
            [[[:key :a] [:index 0] [:key :b]]
             [[:key :a] [:index 1] [:key :b]]])
          [[:key :a] [:multi-index] [:key :b]]))
    (is (=
          (m/combine-paths
            [[[:key :a] [:key :b]]
             [[:key :c] [:key :b]]])
          [[:multi-key] [:key :b]]))
    (is (=
          (m/combine-paths
            [[[:key :a]]])
          [[:key :a]]))))

(m/deflenses {:a ->a
              :b {:c ->c}
              :c {:c1 [->c11 ->c12]}
              :d {:a {:x [->x1 ->x2] :y ->y :z ->z}
                  :b {:x [->x1 ->x2] :y ->y :z ->z}}
              :e [{:p ->ep} {:p ->ep}]
              :f "bob"})

(deftest test-deflenses
  (testing "the deflenses macro"
    (let [data {:a "A"
                :b {:c "C"}
                :c {:c1 ["C1" "C2"]}
                :d {:a {:x [1 2] :y "AY" :z "AZ"}
                    :b {:x [11 12] :y "BY" :z "BZ"}}
                :e [{:p 123} {:p 456}]}]
      (are [lens expected-value] (= (l/view data lens) expected-value)
           ->a    ["A"]
           ->c    ["C"]
           ->c11  ["C1"]
           ->c12  ["C2"]
           ->y    ["AY" "BY"]
           ->z    ["AZ" "BZ"]
           ->x1   [1 11]
           ->x2   [2 12]
           ->ep   [123 456]
       ))))
