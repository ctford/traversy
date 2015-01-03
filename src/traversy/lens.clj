(ns traversy.lens
  (:require [clojure.core.typed :as typed]))

(typed/defalias Seq?
  (typed/TFn [[a :variance :covariant]] (typed/Option (typed/NonEmptyASeq a))))
(typed/defalias Endo (typed/TFn [[a :variance :invariant]] [a -> a]))
(typed/defalias Focus
  (typed/TFn [[a :variance :contravariant] [b :variance :covariant]] [a -> (Seq? b)]))
(typed/defalias Fmap
  (typed/TFn [[a :variance :invariant] [b :variance :invariant]] [(Endo b) a -> a]))
(typed/defalias Lens
  (typed/TFn [[a :variance :invariant] [b :variance :invariant]]
             (typed/HMap :mandatory {:focus (Focus a b) :fmap (Fmap a b)})))

(typed/ann lens (typed/All [a b] [(Focus a b) (Fmap a b) -> (Lens a b)]))
(defn lens
  "Construct a lens from a focus :: x -> seq and an fmap :: f x -> x."
  [focus fmap]
  {:focus focus :fmap fmap})

(typed/ann view (typed/All [a b] [a (Lens a b) -> (Seq? b)]))
(defn view
  "Return a seq of the lens' foci."
  [x lens]
  ((:focus lens) x))

(typed/tc-ignore
(defn view-single
  "Return the sole focus, throwing an error if there are other or no foci."
  [x lens]
  (let [[focus & _ :as foci] (view x lens)
        quantity (count foci)]
    (assert (= 1 quantity) (format "Found %d foci." quantity))
    focus)))

(typed/ann update (typed/All [a b] [a (Lens a b) (Endo b) -> a]))
(defn update
  "Apply f to the foci of x, as specified by lens."
  [x lens f]
  ((:fmap lens) f x))

(typed/ann put (typed/All [a b] [b -> [a -> b]]))
(defn put
  "When supplied as the f to update, sets all the foci to x."
  [x]
  (constantly x))

(def delete-entry
  "When supplied as the f to update an entry, deletes the foci of the lens."
  (put nil))

(typed/ann fapply (typed/All [a] (Fmap a a)))
(defn fapply [f x] (f x))

(typed/ann listify (typed/All [a] (Focus a a)))
(defn listify [x] (seq [x]))

(typed/tc-ignore
(typed/ann it (typed/All [a] (Lens a a)))
(def it
  "The identity lens (under 'combine')."
  (lens listify fapply)))

(typed/ann fconst (typed/All [a b] (Fmap a b)))
(defn fconst [f x] x)

(typed/ann emptify (typed/All [a b] (Focus a b)))
(defn emptify [_] (seq []))

(typed/tc-ignore
(typed/ann nothing (typed/All [a b] (Lens a b)))
(def nothing
  "The null lens. The identity under 'both'."
  (lens emptify fconst)))

(typed/ann zero (typed/All [a] [(typed/Seqable a) -> (typed/Seqable a)]))
(defn zero [x]
  (cond
    (map? x) {}
    (set? x) #{}
    :otherwise []))

(typed/tc-ignore
(typed/ann map-conj (typed/All [a b] [[a -> b] (typed/Seqable a) -> (typed/Seqable b)]))
(defn map-conj [f x] (->> x (map f) (reduce conj (zero x))))

(typed/ann each (typed/All [a] (Lens (typed/Seqable a) a)))
(def each
  "A lens from collection -> item."
  (lens seq map-conj)))

(typed/ann pair (typed/All [a] [typed/AnyInteger a -> (typed/HVec [typed/AnyInteger a])]))
(defn pair [i y] [i y])

(typed/tc-ignore
(typed/ann index (typed/All [a] [(Seq? a) -> (typed/Seqable (typed/HVec [typed/AnyInteger a]))]))
(defn index [xs]
  (map pair (range) xs))

(defn findexed [f x] (map (comp second f) (index x)))

(defn by-key
  "Update a key/value pair's value by looking up the key in 'associative',
  leaving the pair intact if a corresponding value is not found."
  [associative]
  (fn [[k v]] [k (get associative k v)]))

(def indexed
  "A lens from sequence -> index/item pair."
  (lens index findexed)))

(typed/defalias Nthable
  (typed/TFn
    [[a :variance :covariant]]
    (typed/I (typed/Seqable a) typed/Sequential)))

(typed/ann fnth
           (typed/All [a] [typed/AnyInteger
                           [a -> a]
                           (Nthable a)
                           -> (Seq? a)]))
(defn fnth [n f x]
  (seq (concat (seq (take n x)) (seq [(f (nth x n))]) (seq (drop (inc n) x)))))

(typed/tc-ignore
(typed/ann xth (typed/All [a] [typed/AnyInteger -> (Lens (Nthable a) a)]))
(defn xth
  "A lens from collection -> nth item."
  [n]
  (lens (comp seq list #(nth % n)) (partial fnth n))))

(typed/ann clojure.core/update-in (typed/All [k v] [(clojure.lang.Associative k v) (typed/Seqable k) [v -> v]
                                       -> (clojure.lang.Associative k v)]))
(typed/ann fapply-in (typed/All [k v] [(typed/Seqable k) [v -> v] (clojure.lang.Associative k v)
                                       -> (clojure.lang.Associative k v)]))
(defn fapply-in [path f x] (update-in x path f))

(typed/ann curried-fapply-in
           (typed/All [k v] [(typed/Seqable k) ->
                             [[v -> v] (clojure.lang.Associative k v) -> (clojure.lang.Associative k v)]]))
(defn curried-fapply-in [path]
  (fn [f x] (fapply-in path f x)))

(typed/tc-ignore
(typed/ann gets-in (typed/All [k v] [(typed/Seqable k) -> [(clojure.lang.Associative k v) -> (Seq? v)]]))
(defn gets-in [path]
  (fn [x] (seq (list (get-in x path)))))

(typed/ann in (typed/All [k] [(typed/Seqable k) -> (Lens (clojure.lang.Associative k typed/Nothing) typed/Nothing)]))
(defn in
  "A lens from map -> value at path."
  [path]
  (lens (gets-in path) (curried-fapply-in path)))

(defn combine
  "Combine two lenses to form a new lens."
  [outer inner]
  (lens
    (fn [x] (mapcat #(view % inner) (view x outer)))
    (fn [f x] (update x outer #(update % inner f)))))

(defn *>
  "Combine lenses to form a new lens."
  [& lenses]
  (reduce combine it lenses))

(defn fwhen [applies? f x] (if (applies? x) (f x) x))

(defn conditionally
  "A lens to a conditional value."
  [applies?]
  (lens (fn [x] (if (applies? x) [x] []))
        (partial fwhen applies?)))

(defn only
  "A lens from collection -> applicable items."
  [applies?]
  (*> each (conditionally applies?)))

(def maybe
  "A lens to an optional value."
  (conditionally (complement nil?)))

(defn both
  "Combine two lenses in parallel to form a new lens."
  [one another]
  (lens
    (fn [x] (concat (view x one) (view x another)))
    (fn [f x] (-> x (update one f) (update another f)))))

(defn +>
  "Combine lenses in parallel to form a new lens."
  [& lenses]
  (reduce both nothing lenses))

(def all-entries
  "A lens from map -> each entry."
  each)

(def all-values
  "A lens from map -> each value."
  (*> all-entries (in [1])))

(def all-keys
  "A lens from map -> each key."
  (*> all-entries (in [0])))

(defn select-entries
  "A lens from map -> the entries corresponding to ks."
  [ks]
  (only (fn [[k v]] ((set ks) k))))

)
