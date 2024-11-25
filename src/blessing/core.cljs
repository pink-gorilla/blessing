(ns blessing.core
  (:require
   [reagent.core :as r]
   [promesa.core :as p]
   [nano-id.core :refer [nano-id]]
   [ui.flexlayout :refer [create-model layout add-node get-data]]
   [goldly.service.core :refer [clj]]
   [blessing.component.source] ; side effects
   ))

(def model-empty
  {:global {:tabEnableRename true
            :tabEnableClose true
            :tabEnableFloat false
            :tabSetEnableActiveIcon true}
   :layout {:type "row"
            :weight 100
            :children [{:type "tabset"
                        :weight 50
                        :children []}]}
   :borders [{:type "border"
              :size 350
              :location "left"
              :children [{:type "tab"
                          :id "options"
                          :name "Options"
                          :component "option"
                          ;:icon "/r/quanta/adjustments-vertical.svg"
                          :enableClose false}
                         {:type "tab"
                          :id "source"
                          :name "source"
                          :component "source"
                          ;:icon "/r/quanta/adjustments-vertical.svg"
                          :enableClose false}
                          {:type "tab"
                          :id "cells"
                          :name "cells"
                          :component "cells"
                                                   ;:icon "/r/quanta/adjustments-vertical.svg"
                          :enableClose false}
                         
                         ]}]})

(def m (r/atom (create-model
                {:model model-empty
                 :options {}})))

(defonce layout-name-a (atom nil))

(defn save-layout []
  (let [data (get-data @m)]
    (println "saving layout " @layout-name-a)
    (clj 'quanta.studio.layout.core/save-layout @layout-name-a data)))

(def algo-templates-a (r/atom {}))
; keys: template-ids
; vals: {:options :current :views}


(defonce files-a (r/atom []))

#_(-> (clj 'quanta.studio.layout.core/files)
    (p/then (fn [files]
              (println "files: " files)
              (reset! files-a files))))

(defn load-layout [filename]
  (-> (clj 'quanta.studio.layout.core/load-layout filename)
      (p/then (fn [layout]
                (when layout
                  (println "layout: " layout)
                  (reset! m (create-model layout)))))))


(defn add-algo [template-id]
  (when template-id
    (let [template-id-kw (keyword template-id)
          {:keys [options current views]} (template-id-kw @algo-templates-a)
          id (nano-id 5)]
      (println "adding algo: " template-id " id: " id " current:" current)
      (add-node @m {:component "algo"
                   ;:icon "/r/images/article.svg",
                    :name "algo"
                    :id id
                    :options (assoc current
                                    :template-id template-id-kw)
                    :edit options}))))

(defn page [{:keys [route-params query-params handler] :as route}]
  [:div.h-screen.w-screen
   {:style {:display "flex"
            :flex-direction "column"
            :flex-grow 1}}
   [:div {:dir "ltr"
          :style {:margin "2px"
                  :display "flex"
                  :align-items "center"}}

    (into
     [:datalist {:id "files"}]
     (map (fn [fname]
            [:option {:value fname}]) @files-a))

    [:input {:type "text"
             :ref #(set! (.-xxx js/window) %)
             :placeholder "no name"
             :list "files"
             :style {:width "300px"
                     :min-width "300px"
                     :max-width "300px"}
             :on-change (fn [e]
                          (let [v (-> e .-target .-value)]
                            (println "textbox value: " v)
                            (reset! layout-name-a v)
                            (load-layout v)))
             :on-key-up (fn [e]
                          (println "key-up: " e)
                          (when (or (= (.-key e) "Enter")
                                    (= (.-keyCode e) 13))
                            (println "Enter pressed")
                            (save-layout)))}]

    (into [:select {:on-change (fn [e]
                                 (let [v (-> e .-target .-value)]
                                   (println "algo selected: " v)
                                   (add-algo v)
                                   (println "setting index to 0")
                                   ;(set! (.zzz js/window) e)
                                   (set! (-> e .-target .-selectedIndex) "0")
                                   nil))}
           [:option {:value nil :selected true} "< add algo >"]]
          (map (fn [algo-id]
                 [:option {:value (name algo-id) :selected false} (name algo-id)]) (keys @algo-templates-a)))

    [:svg {:fill "none"
           :viewBox "0 0 24 24"
           :stroke-width "1.5"
           :stroke "currentColor"
           :width "24px"
           :height "24px"
           :on-click #(add-node @m {:component "calendar"
                                    :icon "/r/quanta/calendar-days.svg",
                                    :name "calendar"})}
     [:path {:d "M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 0 1 2.25-2.25h13.5A2.25 2.25 0 0 1 21 7.5v11.25m-18 0A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75m-18 0v-7.5A2.25 2.25 0 0 1 5.25 9h13.5A2.25 2.25 0 0 1 21 11.25v7.5m-9-6h.008v.008H12v-.008ZM12 15h.008v.008H12V15Zm0 2.25h.008v.008H12v-.008ZM9.75 15h.008v.008H9.75V15Zm0 2.25h.008v.008H9.75v-.008ZM7.5 15h.008v.008H7.5V15Zm0 2.25h.008v.008H7.5v-.008Zm6.75-4.5h.008v.008h-.008v-.008Zm0 2.25h.008v.008h-.008V15Zm0 2.25h.008v.008h-.008v-.008Zm2.25-4.5h.008v.008H16.5v-.008Zm0 2.25h.008v.008H16.5V15Z"
             :stroke-linecap "round"
             :stroke-linejoin "round"}]]

    [:svg {:fill "none"
           :viewBox "0 0 24 24"
           :stroke-width "1.5"
           :stroke "currentColor"
           :width "24px"
           :height "24px"
           :on-click #(add-node @m {:component "data"
                                    :icon "/r/quanta/bug-ant.svg",
                                    :name "debugger"})}
     [:path {:d "M8.478 1.6a.75.75 0 0 1 .273 1.026 3.72 3.72 0 0 0-.425 1.121c.058.058.118.114.18.168A4.491 4.491 0 0 1 12 2.25c1.413 0 2.673.651 3.497 1.668.06-.054.12-.11.178-.167a3.717 3.717 0 0 0-.426-1.125.75.75 0 1 1 1.298-.752 5.22 5.22 0 0 1 .671 2.046.75.75 0 0 1-.187.582c-.241.27-.505.52-.787.749a4.494 4.494 0 0 1 .216 2.1c-.106.792-.753 1.295-1.417 1.403-.182.03-.364.057-.547.081.152.227.273.476.359.742a23.122 23.122 0 0 0 3.832-.803 23.241 23.241 0 0 0-.345-2.634.75.75 0 0 1 1.474-.28c.21 1.115.348 2.256.404 3.418a.75.75 0 0 1-.516.75c-1.527.499-3.119.854-4.76 1.049-.074.38-.22.735-.423 1.05 2.066.209 4.058.672 5.943 1.358a.75.75 0 0 1 .492.75 24.665 24.665 0 0 1-1.189 6.25.75.75 0 0 1-1.425-.47 23.14 23.14 0 0 0 1.077-5.306c-.5-.169-1.009-.32-1.524-.455.068.234.104.484.104.746 0 3.956-2.521 7.5-6 7.5-3.478 0-6-3.544-6-7.5 0-.262.037-.511.104-.746-.514.135-1.022.286-1.522.455.154 1.838.52 3.616 1.077 5.307a.75.75 0 1 1-1.425.468 24.662 24.662 0 0 1-1.19-6.25.75.75 0 0 1 .493-.749 24.586 24.586 0 0 1 4.964-1.24h.01c.321-.046.644-.085.969-.118a2.983 2.983 0 0 1-.424-1.05 24.614 24.614 0 0 1-4.76-1.05.75.75 0 0 1-.516-.75c.057-1.16.194-2.302.405-3.417a.75.75 0 0 1 1.474.28c-.164.862-.28 1.74-.345 2.634 1.237.371 2.517.642 3.832.803.085-.266.207-.515.359-.742a18.698 18.698 0 0 1-.547-.08c-.664-.11-1.311-.612-1.417-1.404a4.535 4.535 0 0 1 .217-2.103 6.788 6.788 0 0 1-.788-.751.75.75 0 0 1-.187-.583 5.22 5.22 0 0 1 .67-2.04.75.75 0 0 1 1.026-.273Z"
             :stroke-linecap "round"
             :stroke-linejoin "round"
             :fill-rule "evenodd"
             :clip-rule "evenodd"}]]

    [:button
     {:on-click #(add-node @m {:component "url"
                               :name "kibot"
                               :options "https://kibot.com"
                               :id "kibot"})
      :style {:border-radius "5px"
              :border "1px solid lightgray"}}
     "kibot"]

    ;[dt-scroller dt-a]

    ; end of menu div
    ]
   [:div {:style {:display "flex"
                  :flex-grow "1"
                  :position "relative"
                  :border "1px solid #ddd"}}
    [layout @m]]])