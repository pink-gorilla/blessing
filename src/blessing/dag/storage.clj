(ns blessing.dag.storage
  (:require
   [clojure.edn :as edn]
   [babashka.fs :as fs]
   [tick.core :as t]
   [modular.fipp :refer [pprint-str]]))

(defn- dag-filename [{:keys [dir]} dag-id]
  (str dir dag-id ".edn"))

(defn write-dag [this dag]
  (spit
   (dag-filename this (:id dag))
   (pprint-str (assoc dag
                      :save-dt (str (t/instant))))))

(defn create-dag [dag-id]
  {:id dag-id
   :cells {}})

(defn load-dag [this dag-id]
  (let [filename (dag-filename this dag-id)]
    (if  (fs/exists? filename)
      (-> filename 
          slurp
          edn/read-string)
      (create-dag dag-id))))

(defn remove-cell [this dag-id cell-id]
  (let [dag (-> (load-dag this dag-id)
                (update :cells dissoc cell-id))]
      (write-dag this dag)))
    
  
(defn add-cell [this dag-id cell]
  (let [dag (-> (load-dag this dag-id)
                (update :cells assoc (:id cell) cell))]
        (write-dag this dag)))
  







