(ns blessing.component.source
  (:require
   [promesa.core :as p]
   [reagent.core :as r]
   [nano-id.core :refer [nano-id]]
   [ui.flexlayout :refer [component-ui get-data]]
   [ui.frisk :refer [frisk]]
   ;[re-flow.core :refer [flow-ui]]
   [dali.cljviewer :refer [clj-viewer]]
   [dali.container :refer [container-dimension]]
   [goldly.service.core :refer [clj]]
   [rtable.viewer.cheetah :refer [cheetah]]
   ))

#_(defmethod component-ui "help" [{:keys [id]}]
  (fn [options]
    [clj-viewer {:fun  'quanta.dali.plot.md/md
                 :args ["docy/quanta-studio-layout.md"]}]))

#_(defmethod component-ui "calendar" [{:keys [id]}]
  (fn [options]
    [flow-ui {:clj 'quanta.studio.calendar/calendar-time
              :args []
              :render 'quanta.studio.view.calendar/calendar-ui}]))


(defmethod component-ui "data" [{:keys [id state]}]
  (let [data-a (r/atom nil)
        fetch (fn []
                (println "fetching data..")
                (reset! data-a (get-data state)))]
    (fn [options]
      [:div.bg-red-200
       "I can show all the data of the layout:"
       [:br]
       [:button.bg-blue-400.border-round.border {:on-click #(fetch)} "get-data"]
       [:hr]
       "data"
       ;[:hr]
       ;(pr-str @data-a)
       [:hr]
       [frisk @data-a]])))


(defonce cells-a 
  (r/atom {:clock {:id :clock
                   :type :ap 
                   :ap 'demo.flow/clock
                   :show false
                   }}))

(defn cells []
  [:div
   (pr-str (keys @cells-a))])

(defn cells1 []
  [cheetah 
   {:style {:width "100%"
            :height "100%"}
    :columns [{:field "id" :caption "id" :width 80}
              {:field "fn" :caption "fn" :width 160}
              {:field "env?" :caption "env" :width 20}
              {:field "opts?" :caption "opts" :width 20}
              {:field "args" :caption "args" :width 20}
              {:field "show" :caption "show?" :width 50
               :columnType "check" :action "check"}
              
              ]
    :data (or (vals @cells-a) [])
    :watch #(println "watch: " %)
    }])


(defmethod component-ui "cells" [{:keys [id state]}]
  (fn [options]
    [cells1]))



(defonce fns-a (r/atom {}))

(-> (clj 'blessing.core/get-fns)
    (p/then (fn [templates]
              (println "blessing fns: " (keys templates))
              (reset! fns-a templates))))

(defn add-cell [opts]
  (let [id (nano-id 8)
        opts (assoc opts :id id)]
    (swap! cells-a assoc id opts)))

(defn source [opts]
  [:a {:on-click #(add-cell opts)}
   [:p (:fn opts)]])

(defn sources []
  (into [:div]
      (map source (vals @fns-a))))

(defmethod component-ui "source" [{:keys [id state]}]
  (fn [options]
  [sources]
  
  ))



(defmethod component-ui "cell" [{:keys [id state]}]
  (fn [options]
    [:div 
       [:p "cell: " (str id)]
     
     ]))
