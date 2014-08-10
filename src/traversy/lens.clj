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

(defn fapply [f x] (f x))
(def it (lens list fapply))

(def each (lens seq map))
(def eachv (lens seq mapv))
(def elements (lens seq (fn [f x] (->> x (map f) set))))

(defn fnth [n f [x & xs]]
  (if (zero? n)
    (cons (f x) xs)
    (cons x (fnth (dec n) f xs))))

(defn xth [n] (lens (comp list #(nth % n)) (partial fnth n)))

(defn fapply-in [path f x] (update-in x path f))
(defn in [path] (lens (fn [x] (list (get-in x path))) (partial fapply-in path)))

(def all-entries (lens seq (fn [f x] (->> x (map f) (reduce conj {})))))

(defn fwhen [applicable? f x] (if (applicable? x) (f x) x))
(defn fmap-when [applicable? f x] (map (partial fwhen applicable? f) x))
(defn only [applicable?] (lens (fn [x] (filter applicable? x)) (partial fmap-when applicable?)))

(defn combine [outer inner]
  (lens
    (fn [x] (mapcat (partial focus inner) (focus outer x)))
    (fn [f x] (fmap outer (partial fmap inner f) x))))

(def all-values (combine all-entries (in [1])))
(def all-keys (combine all-entries (in [0])))
