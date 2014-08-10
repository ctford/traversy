(ns traversy.lens)

(defprotocol Lens
  (focus [this x])
  (fmap [this f x]))

(defrecord SingleFocus
  [focus fmap]
  Lens
  (focus [_ x] (focus x))
  (fmap [_ f x] (fmap f x)))

(defn view [x lens] (focus lens x))
(defn update [x lens f] (fmap lens f x))

(def it (->SingleFocus identity (fn [f x] (f x))))
(def each (->SingleFocus seq map))
(defn in [path] (->SingleFocus (fn [x] (get-in x path)) (fn [f x] (update-in x path f))))
(def elements (->SingleFocus seq (fn [f x] (->> x (map f) set))))

(defn combine [outer inner]
  (->SingleFocus
    (fn [x] (focus inner (focus outer x)))
    (fn [f x] (fmap outer #(fmap inner f %) x))))
