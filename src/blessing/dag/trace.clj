(ns blessing.dag.trace
  (:require
   [clojure.string :as str]
   [babashka.fs :as fs]
   [taoensso.timbre :as timbre :refer [debug info warn error]]
   [clojure.stacktrace]
   [tick.core :as t]))

(defn ex->str [ex]
  (str
   "\r\n" "\r\n" "\r\n"
   "ex cause: " (ex-cause ex) "\r\n" "\r\n" "\r\n"
   "ex message: " (ex-message ex) "\r\n" "\r\n" "\r\n"
   "stacktrace: " (with-out-str (clojure.stacktrace/print-stack-trace ex))))

(defn write-text [filename text]
  (spit filename text :append true))

(defn write-ex [filename cell-id ex]
  (write-text filename (str "\r\ncell-id: " cell-id "\r\n"
                            (ex->str ex))))

(defn write-edn-raw [filename label v]
  (write-text filename (str "\r\n" label "\r\n"
                            (pr-str v))))

(defn write-edn [filename cell-id v]
  (write-text filename (str "\r\ncell-id: " cell-id "\r\n"
                            (pr-str v))))

(defn setup [path id]
  (let [dt (-> (t/zoned-date-time) (t/in "UTC"))
        dtformat (t/formatter "YYYY-MM-dd-HH-mm-ss")
        filename (str path (t/format dtformat dt) "-" id ".txt")]
    (fs/create-dirs path)
    (info "dag " id " logged to: " filename)
    (write-text filename (str "dag id: " id))
    filename))