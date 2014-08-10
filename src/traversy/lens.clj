(ns traversy.lens)

(defprotocol Lens
  (focus [this x])
  (fmap [this f x]))

(defrecord Single
  [focus fmap]
  Lens
  (focus [_ x] (focus x))
  (fmap [_ f x] (fmap f x)))

(defrecord Multiple
  [focus fmap]
  Lens
  (focus [_ x] (focus x))
  (fmap [_ f x] (fmap f x)))

(defn view [x lens] (focus lens x))
(defn update [x lens f] (fmap lens f x))

(defn fapply [f x] (f x))
(def it (->Single identity fapply))

(def each (->Multiple seq map))
(def eachv (->Multiple seq mapv))
(def elements (->Multiple seq (fn [f x] (->> x (map f) set))))

(defn fapply-in [path f x] (update-in x path f))
(defn in [path] (->Single (fn [x] (get-in x path)) (partial fapply-in path)))

(def all-values (->Multiple vals (fn [f x] (->> x (map #(update-in % [1] f)) (reduce conj {})))))

(defn fwhen [applicable? f x] (if (applicable? x) (f x) x))
(defn fmap-when [applicable? f x] (map (partial fwhen applicable? f) x))
(defn only [applicable?] (->Multiple (fn [x] (filter applicable? x)) (partial fmap-when applicable?)))

(defmulti combine (fn [outer inner] [(class outer) (class inner)]))
(defmethod combine [traversy.lens.Single traversy.lens.Single] [outer inner]
  (->Single
    (fn [x] (focus inner (focus outer x)))
    (fn [f x] (fmap outer (partial fmap inner f) x))))

(defmethod combine [traversy.lens.Single traversy.lens.Multiple] [outer inner]
  (->Multiple
    (fn [x] (focus inner (focus outer x)))
    (fn [f x] (fmap outer (partial fmap inner f) x))))

(defmethod combine [traversy.lens.Multiple traversy.lens.Single] [outer inner]
  (->Multiple
    (fn [x] (map (partial focus inner) (focus outer x)))
    (fn [f x] (fmap outer (partial fmap inner f) x))))

(defmethod combine [traversy.lens.Multiple traversy.lens.Multiple] [outer inner]
  (->Multiple
    (fn [x] (mapcat (partial focus inner) (focus outer x)))
    (fn [f x] (fmap outer (partial fmap inner f) x))))
