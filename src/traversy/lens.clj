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
(def it (->SingleFocus identity fidentity))

(def each (->SingleFocus seq map))

(defn in [path] (->SingleFocus #(get-in % path) #(update-in %2 path %1)))

(def elements (->SingleFocus seq #(->> %2 seq (map %1) set)))
