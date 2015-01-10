# Traversy

[![Build Status](https://travis-ci.org/ctford/traversy.png)](https://travis-ci.org/ctford/traversy)

An experimental encoding of multilenses in Clojure.

# Background

At the 2014 Clojure eXchange [I gave a talk about Lenses in general, and Traversy
specifically](https://skillsmatter.com/skillscasts/6034-journey-through-the-looking-glass).

# Motivation

On a Clojure project, we discovered that changes to the structure of our domain models caused
big waves throughout the codebase. The issue was that the entire structure of large domain objects was encoded
in functions that should only have been concerned with a small part of the whole.

As an illustrative example, imagine we have a bank represented as a nested data structure:

    (def bank {:name "BanCorp"
               :customers [{:account-number "001234":name "Chris" :balance 100}
                           {:account-number "131444":name "Alice" :balance 15000}]})

We might also have a function for crediting customers:

    (defn credit [amount customer]
      (-> customer (update-in [:balance] (partial + amount))))

But the customers are embedded inside the bank data structure. Any attempt to apply a credit to a customer
has to traverse through the bank to find them:

    (defn deposit [bank id amount]
      (let [update-customer (fn [{account-number :account-number :as customer}]
                              (if (= id account-number) (credit amount customer) customer))]
        (-> bank (update-in [:customers] (fn [customers] (map update-customer customers))))))

    (-> bank (deposit "001234" 100))

We can instead build up `deposit`'s understanding of how to apply a credit to a customer out of individual
traversals:

    (defn account-with [id]
      (*> (in [:customers]) (only #(-> % :account-number (= id)))))

`account-with` is a composite traversal, which focuses into the overall structure with `in` and then picks out the
relevant customer with `only`.

    (defn deposit [bank id amount]
      (update bank (account-with id) (partial credit amount)))

    (-> bank (deposit "001234" 100))

As we develop the application, we realise that we need multiple branches, and that we would like to use the
customer's account number as a key in a map to better model our data. We reflect these changes in the traversal:

    (defn account-with [id]
      (*> (in [:branches]) all-values (in [:customers]) (select-entries [id]) (in [1])))

`deposit` can remain unchanged:

    (defn deposit [bank id amount]
      (update bank (account-with id) (partial credit amount)))

    (-> bank (deposit "001234" 100))

What's more, we can use the same traversal to view a customer's account:

    (-> bank (view-single "001234"))

And if we want to reuse parts of the traversal in different contexts, we can break it down further:

    (def customers (*> (in [:branches]) all-values (in [:customers])))
    (defn select-account [id] (*> select-entries [id]) (in [1]))

    (defn account-with [id]
      (*> customers (select-account id)))

We can now view all customer names using the `customers` traversal:

    (-> bank (view (*> customers all-values (in [:name]))))

# Usage

See the [examples](test/traversy/test/lens.clj).

# Laws

Lenses follow some rules that make them behave intuitively:
* `update` then `view` is the same as `view` then `map` - `(-> x (update l f) (view l x)) === (->> x (view l) (map f))`
* An `update` has no effect if passed the `identity` function - `(-> x (update l identity)) === x`
These should hold for any lens `l` that focuses on `x`.

An additional rule should hold, but can be violated when the foci of a lens can shift after an `update`:
*  `(-> x (update l f1) (update l f2)) === (-> (update l (comp f1 f2)))`

An example of this rule being broken is when `only` is used with a predicate and function that interact:
* `(-> [1 2 3] (update (only odd?) inc) (update (only odd?) inc)) => [2 2 4]`
* `(-> [1 2 3] (update (only odd?) (comp inc inc))) => [3 2 5]`
Careful when doing this - and consider documenting any lenses that potentially have this behaviour as unstable.

These rules are based on the [Traversal Laws](http://hackage.haskell.org/package/lens-2.3/docs/Control-Lens-Traversal.html#t:Traversal).

# Current version

0.2.0 is on Clojars.

# FAQs

## Aren't these just degenerate Lenses?

Yes! In fact, they're degenerate
[Traversals](http://hackage.haskell.org/package/lens-2.3/docs/Control-Lens-Traversal.html), with the `Foldable` and
`Functor` instances and without the generality of traversing using arbitrary `Applicatives`.


## Will updates preserve the structure of the target?

Yes. Whether you focus on a map, a set, a vector or a sequence, the structure of the target will remain
the same after an update.

## Can I compose these Lenses with ordinary function composition?

No. Unlike Haskell Lenses, these are not represented as functions. You can, however, use `combine`
(variadic form `*>`) and `both` (variadic form `+>`) to compose lenses.

## How do I run the tests?

`lein midje`.

## Is this stable enough to use in production?

Traversy is in production use on the project it originated from, but the API may yet change.
