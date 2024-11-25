(ns demo.flow
  (:require
   [missionary.core :as m]))

(def clock    ;; A shared process emitting `nil` every second.
  (m/signal
   (m/ap
    (println "creating clock!")
    (m/amb :starting)
    (loop [i 0]
      (m/amb
       (m/? (m/sleep 1000 i))
       ;(println "i: " i)
       (recur (inc i)))))))
