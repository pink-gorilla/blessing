(ns demo.dag
  (:require
   [modular.system]
   [blessing.dag.storage :as s]
   [blessing.dag.runner :as r]))


(def b (modular.system/system :blessing))

b


(s/add-cell b :test {:type :const :id :a :value 5})

(s/add-cell b :test {:type :const :id :b :value 7})

(s/add-cell b :test {:type :atom :id :c :value 3})

(s/add-cell b :test {:type :formula :id :n :fn 'demo.fn/add
                     :args [:a :c]
                     :env? false :sp? false})

(s/add-cell b :test {:type :formula :id :n2 :fn 'demo.fn/borscht
                     :args [:a :c]
                     :env? true :sp? false})


(s/add-cell b :test {:type :formula :id :r :fn 'demo.fn/random
                     :env? false :sp? false})

(s/add-cell b :test {:type :ap :id :clock :ap 'demo.flow/clock})

(s/add-cell b :test {:type :formula :id :clock+c :fn 'demo.fn/add :args [:clock :c]
                     :env? false :sp? false})



(s/remove-cell b :test :b)
(s/remove-cell b :test :r)
(s/remove-cell b :test :clock+b)


(s/load-dag b :test)

(r/start-dag b :test)


(r/start-log-cell b :test :r)

(r/start-log-cell b :test :a)
(r/start-log-cell b :test :c)
(r/start-log-cell b :test :n)
(r/start-log-cell b :test :n2)
(r/start-log-cell b :test :clock)
(r/start-log-cell b :test :clock+c)


