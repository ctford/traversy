(ns traversy.lens)

(defprotocol Lens
  (focus [this x])
  (fmap [this f x]))

(defrecord SingleFocus
  [focus fmap]
  Lens
  (focus [this x] (focus x))
  (fmap [this f x] (fmap f x)))

(defn lens [focus fmap] (->SingleFocus focus fmap))

(defn view [x l] (focus l x))
(defn update [x l f] (fmap l f x))

(defn fidentity [f x] (f x))
(def id (lens identity fidentity))
