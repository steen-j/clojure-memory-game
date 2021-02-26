(ns memorygame.util)

(defn set-toggle
  "Add or remove item `i` from set `S`"
  [S i]
  (let [f (if (contains? S i) disj conj)]
    (f S i)))

(defn randomize-cards "Return a randomized list of the cards" [number-of-cards]
  (->> (range 1 (inc number-of-cards))
       (mapcat #(list % %))
       (shuffle)))

;(defn create-uuid []
;#?(:clj  (java.util.UUID/randomUUID)
;   :cljs (random-uuid)) )

(defn create-uuid "Creates an id-string based on random-uuid" []
  (.toString (random-uuid)))

(defn setup-new-game "Creates a map of the randomized cards" [number-of-different-cards]
  (->> (randomize-cards number-of-different-cards)
       (map (fn [i] (let [id (create-uuid)] {id {:id id :image i :transform #{} :classes #{}}})))
       (into {})))