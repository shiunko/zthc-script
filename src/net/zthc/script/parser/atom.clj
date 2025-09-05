(ns net.zthc.script.parser.atom
  (:require [net.zthc.script.util :as util]
            [clojure.string :as str]))

(defn parse-boolean
  "解析布尔值"
  [s]
  (case s
    ("true" "真") {:type :boolean :value true}
    ("false" "假") {:type :boolean :value false}
    {:type :identifier :value s}))

(defn parse-atom
  "解析原子表达式 (字符串、数字、布尔值)"
  [s]
  (let [s (str/trim s)]
    (cond
      ;; 布尔值
      (contains? #{"true" "false" "真" "假"} s)
      (parse-boolean s)

      ;; 数字
      (re-matches #"-?\d+(\.\d+)?" s)
      {:type :number :value (util/parse-number s)}

      ;; 字符串 (各种引号形式)
      (or (and (str/starts-with? s "\"") (str/ends-with? s "\""))
          (and (str/starts-with? s "'") (str/ends-with? s "'"))
          (and (str/starts-with? s "「") (str/ends-with? s "」"))
          (and (str/starts-with? s "\\") (str/ends-with? s "\\")))
      {:type :string :value (util/remove-quotes s)}

      ;; 标识符
      :else
      {:type :identifier :value s})))

(defn atom?
  "检查是否为原子值"
  [s]
  (let [s (str/trim s)]
    (or
     ;; 布尔值
     (contains? #{"true" "false" "真" "假"} s)
     ;; 数字
     (re-matches #"-?\d+(\.\d+)?" s)
     ;; 字符串
     (or (and (str/starts-with? s "\"") (str/ends-with? s "\""))
         (and (str/starts-with? s "'") (str/ends-with? s "'"))
         (and (str/starts-with? s "「") (str/ends-with? s "」"))
         (and (str/starts-with? s "\\") (str/ends-with? s "\\"))))))
