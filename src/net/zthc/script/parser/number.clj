(ns net.zthc.script.parser.number
  (:require [net.zthc.script.util :as util]
            [clojure.string :as str]))

(defn parse-integer
  "解析整数"
  [s]
  (try
    (let [value (Long/parseLong s)]
      {:type :number
       :subtype :integer
       :value value
       :raw s})
    (catch NumberFormatException e
      (util/log :error "解析整数失败: %s, 错误: %s" s (.getMessage e))
      {:type :number
       :subtype :integer
       :value 0
       :raw s
       :error (.getMessage e)})))

(defn parse-float
  "解析浮点数"
  [s]
  (try
    (let [value (Double/parseDouble s)]
      {:type :number
       :subtype :float
       :value value
       :raw s})
    (catch NumberFormatException e
      (util/log :error "解析浮点数失败: %s, 错误: %s" s (.getMessage e))
      {:type :number
       :subtype :float
       :value 0.0
       :raw s
       :error (.getMessage e)})))

(defn parse-number
  "解析数字文字"
  [s]
  (let [s (str/trim s)]
    (cond
      ;; 整数
      (re-matches #"-?\d+" s)
      (parse-integer s)

      ;; 浮点数
      (re-matches #"-?\d+\.\d+" s)
      (parse-float s)

      ;; 科学计数法
      (re-matches #"-?\d+(\.\d+)?[eE][+-]?\d+" s)
      (parse-float s)

      :else
      {:type :number
       :subtype :invalid
       :value 0
       :raw s
       :error (str "无效的数字格式: " s)})))

(defn number-literal?
  "检查是否为数字文字"
  [s]
  (let [s (str/trim s)]
    (or (re-matches #"-?\d+" s)
        (re-matches #"-?\d+\.\d+" s)
        (re-matches #"-?\d+(\.\d+)?[eE][+-]?\d+" s))))

(defn validate-number
  "验证数字语法"
  [s]
  (let [s (str/trim s)]
    (cond
      (empty? s)
      {:valid false :error "空数字"}

      (not (number-literal? s))
      {:valid false :error "不是有效的数字格式"}

      ;; 检查是否有多个小数点
      (> (count (filter #(= % \.) s)) 1)
      {:valid false :error "数字包含多个小数点"}

      ;; 检查是否以小数点开头或结尾
      (or (str/starts-with? s ".") (str/ends-with? s "."))
      {:valid false :error "数字不能以小数点开头或结尾"}

      :else
      {:valid true})))

(defn format-number
  "格式化数字输出"
  [num]
  (cond
    (integer? num) (str num)
    (and (float? num) (= num (Math/floor num)))
    (str (int num))
    (float? num) (str num)
    :else (str num)))
