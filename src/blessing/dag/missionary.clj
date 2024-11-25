(ns blessing.dag.missionary
  (:require
   [taoensso.telemere :as t]
   [missionary.core :as m]
   [nano-id.core :refer [nano-id]]
   [blessing.dag.trace :as trace]
   ))

; no value

(defrecord no-val [cell-id])

(defn create-no-val [cell-id]
  (no-val. cell-id))

(defn is-no-val? [v]
  (instance? no-val v))

; cell db

(defn add-cell [dag cell-id cell]
  (swap! (:cells dag) assoc cell-id cell
         ;(m/stream cell)
         )
  dag)

(defn get-cell [dag cell-id]
  (get @(:cells dag) cell-id))

(defn cell-ids [dag]
  (-> @(:cells dag) keys))

(defn- get-cell-or-throw [dag cell-id]
  (let [cell (get-cell dag cell-id)]
    (when-not cell
      (throw (ex-info (str "cell-not-found " cell-id) {:cell-id cell-id
                                        :msg "cell not found in dag."})))
    cell))

(defn add-constant-cell [dag cell-id initial-v]
  (add-cell dag cell-id (m/signal (m/seed [initial-v]))))

(defn add-atom-cell [dag cell-id a]
  (add-cell dag cell-id (m/signal (m/watch a))))

;(try
;      ;(warn "run-algo-safe else.. fn: " algo-fn)
;  (algo-fn env spec ds-bars)
;  (catch AssertionError ex (create-error spec ex))
;  (catch Exception ex (create-error spec ex)))

(defn- run-fn [dag cell-id {:keys [fn args env? opts]
                            :or {env? false
                                 opts nil}}]
  (let [env (assoc (:env dag)
                   :cell-id cell-id)]
    (cond
      (and env? opts)
      (apply fn env opts args)

      env?
      (apply fn env args)

      opts
      (apply fn opts args)

      :else
      (apply fn args))))

(defn- calculate [dag cell-id {:keys [sp?]
                               :or {sp? false}
                               :as opts}]
  (m/sp
   ;(t/log! (str "calculate cell-id " cell-id))
   (let [r (run-fn dag cell-id opts)
         r (if sp?
             (m/? r)
             r)]
     ;(t/log! (str "calculate cell-id " cell-id " finished!"))
     ;(t/log! (str "calculate result " r))
     r)))

(defn add-formula-raw-cell [dag cell-id {:keys [fn input] :as opts}]
  (assert dag "dag needs to be non nil")
  (assert (vector? input) ":input needs to be a vector")
  (assert fn ":fn needs to be defined")
  (assert (fn? fn) ":fn needs to be a function")
  (let [input-cells (map #(get-cell-or-throw dag %) input)
        formula-cell-raw (run-fn dag cell-id (assoc opts :args input-cells))]
    (add-cell dag cell-id formula-cell-raw)))

(defn some-input-no-value? [args]
  (some is-no-val? args))

(comment
  (some-input-no-value? [1 2 3])
  (some-input-no-value? [1 2 3 nil])
  (some-input-no-value? [1 2 3 nil (create-no-val :34)])

; 
  )

()

(defn add-formula-cell [dag cell-id {:keys [input] :as opts}]
  (assert dag "dag needs to be non nil")
  (assert (vector? input) "input needs to be a vector")
  (assert (:fn opts) ":fn needs to be defined")
  (assert (or (var? (:fn opts))
              (fn? (:fn opts))) ":fn needs to be a function")
  (let [input-cells (map #(get-cell-or-throw dag %) input)
        ;_ (println "all input cells are good!")
        input-f (apply m/latest vector input-cells)
        formula-result-f (m/ap
                          (m/amb (create-no-val cell-id))
                          (let [args (seq (m/?> input-f))]
                            ;(println "args: " args "sp?: " sp?)
                            (if (some-input-no-value? args)
                              (create-no-val cell-id)
                              (try
                                (let [start (. System (nanoTime))
                                      opts (assoc opts :args args)
                                       ;`result (calculate dag formula-fn args)
                                      ;result (m/? (m/via m/cpu
                                      ;                   ;(if sp?
                                      ;                    ; (m/? (calculate dag cell-id formula-fn args))
                                      ;                     (calculate dag cell-id formula-fn args)))
                                      ;)
                                      result (m/? (calculate dag cell-id opts))
                                      stime (str "\r\ncell " cell-id
                                                 " calculated in "
                                                 (/ (double (- (. System (nanoTime)) start)) 1000000.0)
                                                 " msecs")]
                                  (when (:logger dag)
                                    (trace/write-text (:logger dag) stime))
                                 ;(println "flow result: " result)
                                  result)
                                (catch Exception ex
                                  (t/log! (str "calculate " cell-id "ex: " ex))
                                  (when (:logger dag)
                                    (trace/write-ex (:logger dag) cell-id ex))
                                  (throw ex))))))
        formula-result-f-wrapped (->> formula-result-f
                                      (m/reductions (fn [r v] v) (create-no-val cell-id))
                                      (m/relieve {})
                                      (m/signal))]
    (add-cell dag cell-id formula-result-f-wrapped)))

(defn create-dag
  [{:keys [dir env]
    :or {env {}}}
   dag-id]
   (let [log-dir (str dir "/log/")
         dag {:id dag-id
              :cells (atom {}) 
              :logger (when log-dir
                        (trace/setup log-dir dag-id))
              :tasks (atom {})}]
     (assoc dag :env (merge {:dag dag}
                            env))))

(defn- take-first-val [f]
  ; flows dont implement deref
  (m/eduction
   (remove is-no-val?)
   (take 1)
   f))

(defn- current-v
  "gets the first valid value from the flow"
  [f]
  (m/reduce (fn [r v]
              ;(println "current v: " v " r: " r)
              v) nil
            (take-first-val f)))

(defn get-current-value [dag cell-id]
  (let [cell (get-cell dag cell-id)]
    (m/? (current-v cell))))

(defn -listen
  ; from ribelo/praxis
  "[pure] creates a `listener` for the [[Node]] of the `dag`, every time the
  value of a [[Node]] changes the function is called.


  function `f` should take two arguments, the first is the listener `id`, the
  second is the [[Node]] value. returns a function that allows to delete a
  `listener`


  use as event
  ```clojure
  (emit ::listen! id f)
  ```"
  [>flow f]
  (m/ap
   (m/?> (m/eduction (comp (map (fn [[e v]] (f e v)))) >flow))))

;; TASKS

(defn add-task [dag task-id]
  (swap! (:tasks dag) assoc task-id {:running true :task-id task-id}))

(defn update-task [dag task-id k v]
  (swap! (:tasks dag) assoc-in [task-id k] v))

(defn- get-task [dag task-id]
  (get @(:tasks dag) task-id))

(defn is-running? [dag task-id]
  (when-let [t (get-task dag task-id)]
    (:running t)))

(defn get-dispose-fn [dag task-id]
  (when-let [t (get-task dag task-id)]
    (when (:running t)
      (:dispose-fn t))))

(defn running-tasks [dag]
  (->> @(:tasks dag)
       vals
       (filter #(:running %))
       (map :task-id)))

(defn start!
  "starts a missionary task
   task can be stopped with (stop! task-id).
   useful for working in the repl with tasks"
  [dag cell-id task]
  (add-task dag cell-id)
  (let [on-complete (fn [& args]
                      (update-task dag cell-id :running false)
                      (println "COMPLETED " cell-id)
                      (trace/write-text (:logger dag) (str "\r\nCOMPLETED " cell-id)))
        on-crash (fn [& args]
                   (update-task dag cell-id :running false)
                   (println "\r\nCRASHED " cell-id " args: " args)
                   (trace/write-text (:logger dag) (str "\r\nCRASHED " cell-id "\r\n " args)))
        _   (trace/write-text (:logger dag) (str "\r\n\r\nSTART " cell-id))
        dispose! (task on-complete on-crash)]
    (update-task dag cell-id :dispose-fn dispose!)
    (str "TASK started - use (stop! " cell-id ") to stop this task.")))

(defn stop!
  "stops a missionary task that has been started with start!
    useful for working in the repl with tasks"
  [dag task-id]
  (if-let [dispose-fn (get-dispose-fn dag task-id)]
    (do
      (println "STOP " task-id)
      (trace/write-text (:logger dag) (str "\r\nSTOP " task-id))
      (dispose-fn))
    (println "cannot stop task - not existing!" task-id)))

(defn stop-all!
  "stops all missionary task that have been started with start!
   makes sure that the dag shutsdown corretly"
  [dag]
  (let [task-ids (running-tasks dag)
        n (count task-ids)]
    (if (> n 0)
      (do
        (trace/write-text (:logger dag) (str "\r\nSTOP-ALL " n " tasks: " task-ids))
        (println "STOP-ALL " n " tasks: " task-ids)
        (doall (map #(stop! dag %) task-ids)))
      (do
        (trace/write-text (:logger dag) (str "\r\nSTOP-ALL  - no running tasks"))
        (println "STOP-ALL  - no running tasks")))))

(defn start-log-cell
  "starts logging a missionary flow to a file.
   can be stopped with (stop! id) 
   useful for working in the repl with flows."
  [dag cell-id]
  (if (is-running? dag cell-id)
    (do
      (trace/write-text (:logger dag) (str "\r\n\r\nSTART FAILED " cell-id " is already running."))
      (str "cell " cell-id " is already running - cannot start!"))
    (if-let [cell (get-cell dag cell-id)]
      (let [log-task (m/reduce (fn [r v]
                                 (trace/write-edn (:logger dag) cell-id v)
                                 nil)
                               nil cell)]
        (start! dag cell-id log-task))
      (do
        (trace/write-text (:logger dag) (str "\r\n\r\nSTART FAILED " cell-id " does not exist."))
        (str "cell " cell-id " not found - cannot start!")))))

(defn stop-log-cell [dag cell-id]
  (stop! dag cell-id))