(ns traversy.lens
  (:refer-clojure :exclude [update]))

(defn lens
  "Construct a lens from a focus function and an fmap function:
  (focus x) => a sequence of foci
  (fmap f x) => an updated x"
  [focus fmap]
  {:focus focus :fmap fmap})

(defn view
  "Return a seq of the lens' foci."
  [x lens]
  ((:focus lens) x))

(defn view-single
  "Return the sole focus, throwing an error if there are other or no foci."
  [x lens]
  (let [[focus & _ :as foci] (view x lens)
        quantity (count foci)]
    (assert (= 1 quantity)
            (str "Found " quantity " foci, but expected exactly 1."))
    focus))

(defn update
  "Apply f to the foci of x, as specified by lens."
  [x lens f]
  ((:fmap lens) f x))

(defn put
  "When supplied as the f to update, sets all the foci to x."
  [x]
  (constantly x))

(defn ^:no-doc fapply [f x] (f x))

(def it
  "The identity lens (under 'combine')."
  (lens list fapply))

(defn ^:no-doc fconst [f x] x)

(def nothing
  "The null lens. The identity under 'both'."
  (lens (constantly []) fconst))

(defn ^:no-doc zero [x]
  (cond
    (map? x) {}
    (set? x) #{}
    :otherwise []))

(defn ^:no-doc map-conj [f x] (->> x (map f) (reduce conj (zero x))))

(def each
  "A lens from collection -> item."
  (lens sequence map-conj))

(def ^:no-doc index (partial map vector (range)))
(defn ^:no-doc findexed [f x] (map (comp second f) (index x)))

(def indexed
  "A lens from sequence -> index/item pair."
  (lens index findexed))

(defn ^:no-doc fnth [n f x]
  (concat (take n x) [(f (nth x n))] (drop (inc n) x)))

(defn xth
  "A lens from collection -> nth item."
  ([n]
   (lens (comp list #(nth % n)) (partial fnth n)))
  ([n not-found]
   (lens (comp list #(nth % n not-found)) (partial fnth n))))

(defn ^:no-doc fapply-in [path f x]
  (if (not= (get-in x path ::not-found) ::not-found)
    (update-in x path f)
    x))

(defn in
  "A lens from map -> value at path."
  ([path]
   (in path nil))
  ([path not-found]
   (lens (fn [x] (list (get-in x path not-found))) (partial fapply-in path))))

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

(defn ^:no-doc fwhen [applies? f x] (if (applies? x) (f x) x))

(defn conditionally
  "A lens to a conditional value, based on a predicate.

  This lens is unstable if the predicate interacts with an update."
  [applies?]
  (lens (fn [x] (if (applies? x) [x] []))
        (partial fwhen applies?)))

(defn only
  "A lens from collection -> applicable items, based on a predicate.

  This lens is unstable if the predicate interacts with an update."
  [applies?]
  (*> each (conditionally applies?)))

(def maybe
  "A lens to an optional value.

  This lens is unstable if an update converts nil to another value. "
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
