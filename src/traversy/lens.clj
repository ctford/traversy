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

(def it (->Single identity (fn [f x] (f x))))
(def each (->Multiple seq map))
(def eachv (->Multiple seq mapv))
(defn in [path] (->Single (fn [x] (get-in x path)) (fn [f x] (update-in x path f))))
(def elements (->Multiple seq (fn [f x] (->> x (map f) set))))

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
