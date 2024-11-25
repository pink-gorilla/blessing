(ns blessing.dag.topology)

; stolen from:
; https://blog.exupero.org/topological-sort-in-clojure/

(defn topological-sort [graph]
  (when (seq graph)
    (when-let [depless (seq (keep (fn [[k v]]
                                    (when (empty? v) k))
                                  graph))]
      (concat depless
              (topological-sort
               (into {}
                     (map (fn [[k v]]
                            [k (apply disj v depless)]))
                     (apply dissoc graph depless)))))))

(comment

  (topological-sort
   {:a #{:b :c :d}
    :b #{:c :d}
    :c #{:d :e}
    :d #{}
    :e #{:d}})
;; => (:d :e :c :b :a)

  (topological-sort
   {:a #{:b :c :d}
    :b #{:c :d}
    :c #{:d :e}
    :d #{:a}
    :e #{:d}})
;; => nil
  ; note that d depends on a  and a depends on d

;
  )