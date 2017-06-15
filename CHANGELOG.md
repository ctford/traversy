0.5.0
-----
* 'in' distinguishes between absence of value (empty list returned) and nil value (nil returned)

0.4.0
-----
* Support ClojureScript.
* Upgrade to Clojure 1.7 to use reader conditionals in the tests.

0.3.1
-----
* `xth` accepts a default value if the index is out of bounds.

0.3.0
-----

* Removed `delete` on map entry lenses.
* `in` does nothing if the path does not exist (unlike `update-in`).
* Added `indexed` and `conditionally` lenses.
* `view-single` throws an error if there are no foci.

0.2.0
-----

* `view` renamed to `view-single`, and `view-all` renamed to `view`.
* Confined deletion to entries in a map, as it's dangerous for sets and seqs.

0.1.0
-----

* First version.
