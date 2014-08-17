# Traversy

[![Build Status](https://travis-ci.org/ctford/traversy.png)](https://travis-ci.org/ctford/traversy)

An experimental encoding of multilenses in Clojure.

# Usage

See the tests.

# FAQs

## Aren't these just degenerate Lenses?

Yes! In fact, they're [Traversals](http://hackage.haskell.org/package/lens-2.3/docs/Control-Lens-Traversal.html).

## So they obey the [Traversal Laws](http://hackage.haskell.org/package/lens-2.3/docs/Control-Lens-Traversal.html#t:Traversal)?

Not entirely. Lenses with unstable foci like `only` violate sequential composition
i.e. `(update x l (comp f2 f1))` is not always equivalent to `(update (update x l f1) l f2)`.

## Can you use Lenses to filter?

Yes. Lenses interpret a `nil` returned from an update as a deletion.

This also violates sequential composition.

## Can I compose these Lenses with ordinary function composition?

No. Unlike Haskell Lenses, these are not represented as functions. You can, however, use `combine`
and the variadic `+>`.
