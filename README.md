# Traversy

An experimental encoding of lenses and multilenses.

# Usage

See the tests.

# Design dilemmas

* How should we handle composing viewing through multilenses (Traversals)?
* Should the construction of lenses be from get and set rather than get and update?
* Should the representation of lenses be function-based a la Haskell?
* Should we admit lenses like `only` that don't obey the Traversal laws?
