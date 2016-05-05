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
          :else (throw (Exception. "Don't know what to do")))
        (cons (combine-paths (map rest paths)))))))

(defn stage-to-lens-aot [[type key]]
  (case type
    :key `(traversy.lens/in [~key])
    :index `(traversy.lens/xth ~key)
    :multi-key 'traversy.lens/all-values
    :multi-index 'traversy.lens/each))

(defn path-to-lens-aot [p]
  (let [lenses (->> p
                    drop-last
                    (map stage-to-lens-aot))]
    `(traversy.lens/*> ~@lenses)))

(defn convert-path-group-to-lens-entry [[k v]]
  [(second k)
   (-> v combine-paths path-to-lens-aot)])

(defn gen-lenses [m id?]
  (->> (get-paths m id?)
       (group-by last)
       (map convert-path-group-to-lens-entry)
       (into {})))

(defmacro deflenses [m]
  `(do
     ~@(for [[s l] (gen-lenses m symbol?)]
         `(def ~s ~l))))
