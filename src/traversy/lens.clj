(ns traversy.lens)

(defn lens
  "Construct a lens from a focus :: x -> seq and an fmap :: f x -> x."
  [focus fmap]
  {:focus focus :fmap fmap})

(defn collect
  "Return a seq of the lens' foci."
  [x lens]
  ((:focus lens) x))

(defn view
  "Return a single focus, ignoring any subsequent foci."
  [x lens]
  (first (collect x lens)))

(defn update
  "Apply f to the foci of x, as specified by lens."
  [x lens f]
  ((:fmap lens) f x))

(defn put
  "When supplied as the f to update, sets all the foci to x."
  [x]
  (constantly x))

(def delete
  "When supplied as the f to update, deletes the foci of the lens."
  (put nil))

(defn fapply [f x] (f x))

(def it
  "The identity lens."
  (lens list fapply))

(def null
  "The null lens."
  (lens (constantly []) (fn [f x] x)))

(defn zero [x]
  (cond
    (vector? x) []
    (set? x) #{}
    (map? x) {}
    :otherwise []))

(defn map-conj [f x] (->> x (map f) (filter (complement nil?)) (reduce conj (zero x))))

(def each
  "A lens from collection -> item."
  (lens seq map-conj))

(defn fnth [n f [x & xs]]
  (if (zero? n)
    (cons (f x) xs)
    (cons x (fnth (dec n) f xs))))

(defn xth
  "A lens from collection -> nth item."
  [n]
  (lens (comp list #(nth % n)) (partial fnth n)))

(defn fapply-in [path f x] (update-in x path f))

(defn in
  "A lens from map -> value at path."
  [path]
  (lens (fn [x] (list (get-in x path))) (partial fapply-in path)))

(defn fwhen [applicable? f x] (if (applicable? x) (f x) x))
(defn fsome [applicable? f x] (map-conj (partial fwhen applicable? f) x))

(defn only
  "A lens from collection -> applicable items."
  [applicable?]
  (lens (partial filter applicable?) (partial fsome applicable?)))

(defn combine
  "Combine two lenses to form a new lens."
  [outer inner]
  (lens
    (fn [x] (mapcat #(collect % inner) (collect x outer)))
    (fn [f x] (update x outer #(update % inner f)))))

(defn +>
  "Combine lenses to form a new lens."
  [& lenses]
  (reduce combine it lenses))

(defn both
  "Combine two lenses in parallel to form a new lens."
  [one another]
  (lens
    (fn [x] (concat (collect x one) (collect x another)))
    (fn [f x] (-> x (update one f) (update another f)))))

(defn *>
  "Combine lenses in parallel to form a new lens."
  [& lenses]
  (reduce both null lenses))

(def all-entries
  "A lens from map -> each entry."
  each)

(def all-values
  "A lens from map -> each value."
  (+> all-entries (in [1])))

(def all-keys
  "A lens from map -> each key."
  (+> all-entries (in [0])))

(defn select-entries
  "A lens from map -> the entries corresponding to ks."
  [ks]
  (only (fn [[k v]] ((set ks) k))))
