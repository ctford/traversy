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

See the [tests](test/traversy/test/lens.clj).

# Current version

0.2.0 is on Clojars.

# FAQs

## Aren't these just degenerate Lenses?

Yes! In fact, they're degenerate
[Traversals](http://hackage.haskell.org/package/lens-2.3/docs/Control-Lens-Traversal.html), with the `Foldable` and
`Functor` instances and without the generality of traversing using arbitrary `Applicatives`.

## So they obey the [Traversal Laws](http://hackage.haskell.org/package/lens-2.3/docs/Control-Lens-Traversal.html#t:Traversal)?

Not entirely. Lenses with unstable foci like `only` violate sequential composition
i.e. `(update x l (comp f2 f1))` is not always equivalent to `(update (update x l f1) l f2)`.

## Can you use Lenses to filter?

Only if you're working with maps, presently. Lenses interpret a `nil` returned from an update as a deletion.

This also violates sequential composition.

## Will updates preserve the structure of the target?

Yes. Whether you focus on a map, a set, a vector or a sequence, the structure of the target will remain
the same after an update.

## Can I compose these Lenses with ordinary function composition?

No. Unlike Haskell Lenses, these are not represented as functions. You can, however, use `combine`
and the variadic `*>`.

## How do I run the tests?

`lein midje`.

## Is this stable enough to use in production?

No. The design is still in flux, and the API may change radically.
