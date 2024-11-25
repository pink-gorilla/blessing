(ns demo.fn)


(defn add [a b]
  (+ a b))

(defn borscht [{:keys [x] :as env} a b]
  (+ a b))

(defn random []
  (rand-int 100))