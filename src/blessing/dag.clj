(ns blessing.dag
  (:require
   [blessing.dag.impl :as dag]
   [blessing.dag.trace :refer [write-edn-raw]]
   [quanta.algo.dag.spec :refer [spec->ops]]))

;; algo

(defn- add-cell [d time-fn [cell-id {:keys [calendar formula formula-raw
                                            sp? env?]
                                     :as opts}]]
  (let [cell-opts (dissoc opts :fn :calendar :formula :formula-raw
                          :sp? :env?)]
    (cond
      calendar
      (do (dag/add-cell d calendar (time-fn calendar))
          (dag/add-formula-cell d cell-id (assoc opts
                                                 :input [calendar]
                                                 :opts (assoc cell-opts :calendar calendar)
                                                 :sp? (if (boolean? sp?) sp? true) ; default true
                                                 :env? (if (boolean? env?) env? true) ; default true
                                                 )))

      formula
      (dag/add-formula-cell d cell-id (assoc opts
                                             :input formula
                                             :opts cell-opts
                                             :sp? (if (boolean? sp?) sp? false) ; default false
                                             :env? (if (boolean? env?) env? false) ; default false
                                             ))

      formula-raw
      (dag/add-formula-raw-cell d cell-id (assoc opts
                                                 :input formula-raw
                                                 :opts cell-opts
                                                 ; sp? does not apply here.
                                                 :env? (if (boolean? env?) env? false) ; default false
                                                 )))))

(defn- add-cells [d time-fn cell-spec]
  (doall (map #(add-cell d time-fn %) cell-spec)))

(defn add-algo [dag algo-spec]
  (let [cell-spec (spec->ops algo-spec)
        {:keys [time-fn]} dag]
    (assert time-fn "algo can only be added after time-env has been set")
    (write-edn-raw (:logger dag) "\r\nadded-algo" cell-spec)
    (add-cells dag time-fn cell-spec)
    dag))
