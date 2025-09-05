(ns net.zthc.script.util
  (:require [clojure.string :as str])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn log
  "日志输出函数，支持格式化字符串"
  [level format-str & args]
  (let [timestamp (.format (LocalDateTime/now)
                           (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))
        level-str (str/upper-case (name level))
        message (if args
                  (apply format format-str args)
                  format-str)]
    (println (format "[%s] %s - %s" timestamp level-str message))))

(defn big-first
  "将字符串首字母大写"
  [s]
  (if (empty? s)
    s
    (str (str/upper-case (first s)) (str/lower-case (subs s 1)))))

(defn remove-quotes
  "移除字符串两边的引号"
  [s]
  (if (and (>= (count s) 2)
           (or (and (str/starts-with? s "\"") (str/ends-with? s "\""))
               (and (str/starts-with? s "'") (str/ends-with? s "'"))
               (and (str/starts-with? s "「") (str/ends-with? s "」"))
               (and (str/starts-with? s "\\") (str/ends-with? s "\\"))))
    (subs s 1 (dec (count s)))
    s))

(defn safe-read-string
  "安全地读取字符串为 Clojure 数据结构"
  [s]
  (try
    (read-string s)
    (catch Exception e
      (log :warn "无法解析字符串: %s, 错误: %s" s (.getMessage e))
      s)))

(defn parse-number
  "解析数字字符串"
  [s]
  (try
    (cond
      (re-matches #"-?\d+" s) (Long/parseLong s)
      (re-matches #"-?\d+\.\d+" s) (Double/parseDouble s)
      :else s)
    (catch Exception e
      (log :warn "无法解析数字: %s, 错误: %s" s (.getMessage e))
      s)))

(defn chinese-keyword?
  "检查是否为中文关键字"
  [s]
  (contains? #{"函数" "变量" "常量" "如果" "否则" "循环" "返回" "结束"} s))

(defn normalize-chinese-quotes
  "标准化中文引号为英文引号"
  [s]
  (-> s
      (str/replace "「" "\"")
      (str/replace "」" "\"")
      (str/replace "\\" "\"")
      (str/replace "\\" "\"")))

(defn extract-function-name
  "从函数调用中提取函数名"
  [s]
  (when (str/starts-with? s "@")
    (let [name-part (subs s 1)
          paren-idx (str/index-of name-part "(")]
      (if paren-idx
        (subs name-part 0 paren-idx)
        name-part))))

(defn split-function-args
  "分割函数参数，考虑嵌套括号"
  [args-str]
  (let [args-str (str/trim args-str)]
    (if (empty? args-str)
      []
      (loop [chars (seq args-str)
             current-arg ""
             args []
             paren-depth 0
             in-quotes false
             quote-char nil]
        (if (empty? chars)
          (conj args (str/trim current-arg))
          (let [c (first chars)
                rest-chars (rest chars)]
            (cond
              ;; 处理引号
              (and (not in-quotes) (contains? #{\' \" \「 \\} c))
              (recur rest-chars (str current-arg c) args paren-depth true c)

              (and in-quotes (= c quote-char))
              (recur rest-chars (str current-arg c) args paren-depth false nil)

              ;; 在引号内，直接添加字符
              in-quotes
              (recur rest-chars (str current-arg c) args paren-depth in-quotes quote-char)

              ;; 处理括号
              (= c \()
              (recur rest-chars (str current-arg c) args (inc paren-depth) in-quotes quote-char)

              (= c \))
              (recur rest-chars (str current-arg c) args (dec paren-depth) in-quotes quote-char)

              ;; 处理逗号分隔符
              (and (= c \,) (= paren-depth 0))
              (recur rest-chars "" (conj args (str/trim current-arg)) paren-depth in-quotes quote-char)

              ;; 其他字符
              :else
              (recur rest-chars (str current-arg c) args paren-depth in-quotes quote-char))))))))

(defn format-error
  "格式化错误信息"
  [error-type message & args]
  {:error-type error-type
   :message    (if args (apply format message args) message)
   :timestamp  (System/currentTimeMillis)})

(defn debug-print
  "调试打印函数"
  [label value]
  (log :debug "%s: %s" label (pr-str value))
  value)
