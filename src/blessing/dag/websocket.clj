(ns blessing.dag.websocket
   (:require
    [taoensso.timbre :refer [info error]]
    [modular.ws.msg-handler :refer [-event-msg-handler]]
    ;[modular.permission.session :refer [get-user]]
    ;[clj-service.executor :refer [execute-with-binding]]
    [dali.spec :refer [dali-spec?]]
    [dali.plot.text :refer [text]]
    [blessing.dag.runner :as runner]
    [blessing.dag.missionary :as d]
    ))

 
(defn default-ui [v]
  (text {:text (str "default-ui: \r\n" 
                    (pr-str v))}))


 (defn start-subscriptions! [this]
  (defmethod -event-msg-handler :blessing/subscribe
    [{:keys [event _id _?data uid send-fn] :as req}]
    (let [[_ topic] event ; _ is :blessing/subscribe
          {:keys [dag-id cell-id]} topic
          _ (info "blessing subscribe: dag-id " dag-id " cell-id " cell-id " uid " uid)
          user nil ; (get-user permission-service uid)
          publish-fn (fn [v]
                        (let [dali-val (cond 
                                         (d/is-no-val? v)
                                         (default-ui ":no-val")

                                         (dali-spec? v)
                                         v
                                         :else
                                         (default-ui v))] 
                          (info "sending to cell: " cell-id " dali-val: " dali-val)
                        (send-fn uid [:blessing/topic
                                      {:dag-id dag-id
                                       :cell-id cell-id
                                       :dali dali-val}
                                      ])))
          task (runner/start-dali-cell this dag-id cell-id publish-fn)]
      nil)))


 (defmethod -event-msg-handler :blessing/unsubscribe
   [{:keys [event _id ?data uid] :as req}]
   (let [;[_ params] event ; _ is :clj/re-flow
         ;{:keys [fun args]} params
         ;user (get-user permission-service uid)
         ;topic {:clj fun :args args}
         _ (info "blessing/unsubscribe " ?data)
         ;task (get @tasks topic)
         ]
     ;(if task
       ;(do (info "disposing of task " topic)
           ;(task) ; dispose of the task  
           ;)
       ;(error "no task found to dispose: " topic))
     ;(swap! tasks dissoc topic)
     ))
   