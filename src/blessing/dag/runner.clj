(ns blessing.dag.runner
  (:require
   [taoensso.timbre :refer [info error]]
   [missionary.core :as m]
   [blessing.dag.storage :as storage]
   [blessing.dag.missionary :as d]))

(defmulti add-cell
  (fn [live-dag {:keys [type] :as opts}] type))

(defmethod add-cell :const
  [live-dag {:keys [id value]}]
  (d/add-constant-cell live-dag id value))

(defmethod add-cell :atom
  [live-dag {:keys [id value]}]
  (d/add-atom-cell live-dag id (atom value)))


(defmethod add-cell :ap
  [live-dag {:keys [id ap]}]
  (let [ap* (requiring-resolve ap)]
  (println "ap: " ap " ap* " ap*)
  (d/add-cell live-dag id ap*)))


(defmethod add-cell :formula
  [live-dag {:keys [id fn env? sp? args]
             :or {args []}
             :as opts}]
  (let [fn* (requiring-resolve fn)
        arglists (-> fn* meta :arglists)
        ]
    (println "symbol: " fn " fn: " fn*)
    (println "arglists: " arglists)
    (d/add-formula-cell
     live-dag
     id
     (assoc opts
            :fn fn*
            :input args
            :sp? (if (boolean? sp?) sp? false) ; default false
            :env? (if (boolean? env?) env? false) ; default false
            ))))


(defn remove-cell [this live-dag cell-id]
  (println "not implemented"))


(defn start-dag [{:keys [runner] :as this} dag-id]
  (let [live (d/create-dag this dag-id)
        dag (storage/load-dag this dag-id)]
    (doall (map #(add-cell live %) (-> dag :cells vals)))
    (swap! runner assoc dag-id live)))

(defn stop-dag [{:keys [runner] :as this} dag-id]
  (let [live (get @runner dag-id)]
    (d/stop-all! live)
    (swap! runner dissoc dag-id)))

(defn get-dag [{:keys [runner] :as this} dag-id]
  (if-let [live (get @runner dag-id)]
    live
    (start-dag this dag-id)))


(defn start-log-cell [this dag-id cell-id]
  (let [live (get-dag this dag-id)]
    (d/start-log-cell live cell-id)))

(defn start-dali-cell
  [this dag-id cell-id publish-fn]
   (let [dag (get-dag this dag-id)
         cell (d/get-cell dag cell-id)]
  (if (and dag cell)
    (let [dali-task (m/reduce (fn [_r v]
                                (publish-fn v)
                                nil)
                              nil cell)]
      (info "start dali-cell dag:" dag-id " cell: " cell-id)
      (d/start! dag cell-id dali-task))
    (str "cell " cell-id " not found - cannot start!"))))