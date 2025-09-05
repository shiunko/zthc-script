(ns net.zthc.script.parser.string
  (:require [net.zthc.script.util :as util]
            [clojure.string :as str]))

(defn normalize-quotes
  "标准化各种引号为统一格式"
  [s]
  (-> s
      (str/replace "「" "\"")
      (str/replace "」" "\"")
      (str/replace "\\\\" "\"")))

(defn extract-string-content
  "提取字符串内容，移除引号"
  [s]
  (let [s (str/trim s)]
    (cond
      ;; 双引号字符串
      (and (str/starts-with? s "\"") (str/ends-with? s "\""))
      (subs s 1 (dec (count s)))

      ;; 单引号字符串
      (and (str/starts-with? s "'") (str/ends-with? s "'"))
      (subs s 1 (dec (count s)))

      ;; 中文引号
      (and (str/starts-with? s "「") (str/ends-with? s "」"))
      (subs s 1 (dec (count s)))

      ;; 反斜杠引号
      (and (str/starts-with? s "\\") (str/ends-with? s "\\"))
      (subs s 1 (dec (count s)))

      :else s)))

(defn parse-escape-sequences
  "解析转义序列"
  [s]
  (-> s
      (str/replace "\\n" "\n")
      (str/replace "\\t" "\t")
      (str/replace "\\r" "\r")
      (str/replace "\\\"" "\"")
      (str/replace "\\'" "'")
      (str/replace "\\\\" "\\")))

(defn parse-string
  "解析字符串文字"
  [s]
  (try
    (let [content (extract-string-content s)]
      {:type :string
       :value (parse-escape-sequences content)
       :raw s})
    (catch Exception e
      (util/log :error "解析字符串失败: %s, 错误: %s" s (.getMessage e))
      {:type :string
       :value s
       :raw s
       :error (.getMessage e)})))

(defn string-literal?
  "检查是否为字符串文字"
  [s]
  (let [s (str/trim s)]
    (or (and (str/starts-with? s "\"") (str/ends-with? s "\""))
        (and (str/starts-with? s "'") (str/ends-with? s "'"))
        (and (str/starts-with? s "「") (str/ends-with? s "」"))
        (and (str/starts-with? s "\\") (str/ends-with? s "\\")))))

(defn validate-string
  "验证字符串语法"
  [s]
  (let [s (str/trim s)]
    (cond
      (empty? s)
      {:valid false :error "空字符串"}

      (not (string-literal? s))
      {:valid false :error "不是有效的字符串文字"}

      ;; 检查引号匹配
      (or (and (str/starts-with? s "\"") (not (str/ends-with? s "\"")))
          (and (str/starts-with? s "'") (not (str/ends-with? s "'")))
          (and (str/starts-with? s "「") (not (str/ends-with? s "」")))
          (and (str/starts-with? s "\\") (not (str/ends-with? s "\\"))))
      {:valid false :error "引号不匹配"}

      :else
      {:valid true})))
