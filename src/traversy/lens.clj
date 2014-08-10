(ns traversy.lens)

(defprotocol Lens
  (focus [this x])
  (fmap [this f x]))

(defrecord SingleFocus
  [focus fmap]
  Lens
  (focus [this x] (focus x))
  (fmap [this f x] (fmap f x)))

(defn view [x lens] (focus lens x))
(defn update [x lens f] (fmap lens f x))

(defn fidentity [f x] (f x))
(def id (->SingleFocus identity fidentity))

(def each (->SingleFocus seq map))
