0.3.0
-----

* Removed `delete` on map entry lenses.
* `in` does nothing if the path does not exist (unlike `update-in`).
* Added `by-key`, `indexed` and `conditionally` lenses.
* `view-single` throws an error if there are no foci.

0.2.0
-----

* `view` renamed to `view-single`, and `view-all` renamed to `view`.
* Confined deletion to entries in a map, as it's dangerous for sets and seqs.

0.1.0
-----

* First version.
