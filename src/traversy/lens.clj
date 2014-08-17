(ns traversy.lens)

(defprotocol Lens
  (focus [_ x])
  (fmap [_ f x]))

(defn lens [focus fmap]
  (reify
    Lens
    (focus [_ x] (focus x))
    (fmap [_ f x] (fmap f x))))

(defn collect [x lens] (focus lens x))
(defn view [x lens] (first (collect x lens)))
(defn update [x lens f] (fmap lens f x))
(def delete (constantly nil))

(defn fapply [f x] (f x))
(def it (lens list fapply))

(defn zero [x]
  (cond
    (vector? x) []
    (set? x) #{}
    (map? x) {}
    :otherwise '()))

(defn map-conj [f x] (->> x (map f) (filter (complement nil?)) (reduce conj (zero x))))
(def each (lens seq map-conj))

(defn fnth [n f [x & xs]]
  (if (zero? n)
    (cons (f x) xs)
    (cons x (fnth (dec n) f xs))))
(defn xth [n] (lens (comp list #(nth % n)) (partial fnth n)))

(defn fapply-in [path f x] (update-in x path f))
(defn in [path] (lens (fn [x] (list (get-in x path))) (partial fapply-in path)))

(defn fwhen [applicable? f x] (if (applicable? x) (f x) x))
(defn fsome [applicable? f x] (map-conj (partial fwhen applicable? f) x))
(defn only [applicable?] (lens (partial filter applicable?) #(->> %2 (fsome applicable? %1))))

(defn combine [outer inner]
  (lens
    (fn [x] (mapcat (partial focus inner) (focus outer x)))
    (fn [f x] (fmap outer (partial fmap inner f) x))))
(defn +> [& ls] (reduce combine it ls))

(def all-entries (lens seq map-conj))
(def all-values (+> all-entries (in [1])))
(def all-keys (+> all-entries (in [0])))
(defn select-entries [ks]
  (let [applicable? (fn [[k v]] ((set ks) k))]
    (lens
      #(-> % (select-keys ks) seq)
      (fn [f x] (map-conj (partial fwhen applicable? f) x)))))
