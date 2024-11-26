(ns blessing.dag.websocket
   (:require
    [missionary.core :as m]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [modular.ws.core :refer [send!]]
    [modular.ws.msg-handler :refer [-event-msg-handler]]
))

(defonce subscriptions (atom {}))

(defn subscription [{:keys [dag-id cell-id] :as topic}]
  (m/observe
   (fn [!]
     (println "blessing/subscribe " topic)
     (swap! subscriptions assoc topic !)
     (send! [:blessing/subscribe topic])
     (fn []
       (println "blessing/unsubscribe " topic)
       (send! [:blessing/unsubscribe topic])
       (swap! subscriptions dissoc topic)
       ))))

(defmethod -event-msg-handler :blessing/topic
  [{:as _msg :keys [?data]}]
  ; ?data:
  ;{:dag-id :test, 
  ; :cell-id :clock, 
  ; :dali {:viewer-fn 'dali.viewer.text/text, :data {:text "default-ui: 56"}}}
  ;(println "blessing/data: " ?data)
  (if (map? ?data)
    (let [{:keys [dag-id cell-id dali]} ?data
          topic {:dag-id dag-id :cell-id cell-id}
          ! (get @subscriptions topic)]
      (if !
        (do ;(println "re-flowing topic:" topic dali)
            (! dali))
        (println "not reflowing. no ! for topic: " topic)))
    (println "data no map: " ?data)))