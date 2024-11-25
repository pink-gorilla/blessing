(ns blessing.core
  (:require
   [taoensso.timbre :refer [info warn error]]
   [taoensso.telemere :as tm]
   [babashka.fs :as fs]
   [clj-service.core :refer [expose-functions]]))

(defn start-blessing
  "calculate are the dag opts {:log-dir :env}"
  [{:keys [exts clj role
           dir env]
    :or {dir ".data/public/blessing/"
         env {}}}]
  (tm/log! "starting blessing..")
  ; this assert fucks up the starting of the clip system
  ;(assert calculate "studio needs :calculate (calculation-dag settings)")
  (let [this {:dir dir
              :fns (atom {})
              :runner (atom {})
              :env env
              }]
    (when dir
      (tm/log! (str "ensuring blessing-dir: " dir))
      (fs/create-dirs dir))
    (if clj
      (do
        (tm/log! "starting blessing clj-services..")
        (expose-functions clj
                          {:name "blessing"
                           :symbols ['blessing.core/get-fns
                                     
                                     ]
                           :permission role
                           :fixed-args [this]}))
      (warn "blessing starting without clj-services, perhaps you want to pass :clj key"))
    (info "blessing running!")
    this))

(defn register-fn
  [{:keys [fns]} {:keys [fn] :as opts}]
  (swap! fns assoc fn opts))


(defn get-fns
  [{:keys [fns]}]
  @fns)



