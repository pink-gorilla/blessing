(ns demo.init
  (:require 
   [blessing.core :as blessing]))


(defn init [b]
  (println "demo.init..")

  (blessing/register-fn
   b
   {:fn 'demo.fn/random
    :args []
    :env? false
    :opts? false})

  )