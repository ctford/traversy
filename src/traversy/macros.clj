(ns traversy.macros
  (:require [traversy.lens :as l]))

(defn get-paths [n id?]
  (cond (id? n) [[[:val n]]]
        (map? n) (mapcat (fn [[k v]] (map (partial cons [:key k]) (get-paths v id?))) n)
        (sequential? n) (apply concat (map-indexed (fn [k v] (map (partial cons [:index k]) (get-paths v id?))) n))
        :else []))

(defn combine-paths [paths]
  (if (empty? (first paths))
    []
    (let [heads (map first paths)]
      (->
        (cond
          (apply = heads) (first heads)
          (= (ffirst heads) :key) [:multi-key]
          (= (ffirst heads) :index) [:multi-index]
          :else (first heads))
        (cons (combine-paths (map rest paths)))))))

(defn stage-to-lens [[type key]]
  (case type
    :key (l/in [key])
    :index (l/xth key)
    :multi-key l/all-values
    :multi-index l/each))

(defn path-to-lens [p]
  (->> p
       drop-last
       (map stage-to-lens)
       (apply l/*>)))

(defn convert-path-group-to-lens-entry [[k v]]
  [(second k)
   (-> v combine-paths path-to-lens)])

(defn gen-lenses [m id?]
  (->> (get-paths m id?)
       (group-by last)
       (map convert-path-group-to-lens-entry)
       (into {})))

(defmacro deflenses [m]
  `(do
     ~@(for [[s l] (gen-lenses m symbol?)]
         `(def ~s ~l))))
