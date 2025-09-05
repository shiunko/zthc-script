(ns net.zthc.script.builtins
  (:require [net.zthc.script.util :as util]
            [clojure.string :as str]))

(defn 调试输出
  "调试输出函数"
  [& args]
  (let [output (str/join " " (map str args))]
    (util/log :info "调试输出: %s" output)
    (println output)
    output))

(defn 调试输出_
  "内联调试输出函数"
  [& args]
  (let [output (str/join " " (map str args))]
    (print output)
    (flush)
    output))

(defn 到整数
  "转换为整数"
  [value]
  (try
    (cond
      (integer? value) value
      (number? value) (int value)
      (string? value) (Integer/parseInt (str/trim value))
      :else (throw (IllegalArgumentException. (str "无法转换为整数: " value))))
    (catch Exception e
      (util/log :error "转换为整数失败: %s" (.getMessage e))
      0)))

(defn 到字符串
  "转换为字符串"
  [value]
  (str value))

(defn 到浮点数
  "转换为浮点数"
  [value]
  (try
    (cond
      (number? value) (double value)
      (string? value) (Double/parseDouble (str/trim value))
      :else (throw (IllegalArgumentException. (str "无法转换为浮点数: " value))))
    (catch Exception e
      (util/log :error "转换为浮点数失败: %s" (.getMessage e))
      0.0)))

(defn 返回
  "返回函数"
  [value]
  value)

(defn 打印
  "打印函数"
  [& args]
  (let [output (str/join " " (map str args))]
    (println output)
    output))

(defn 输出
  "输出函数（不换行）"
  [& args]
  (let [output (str/join " " (map str args))]
    (print output)
    (flush)
    output))

(defn 输入
  "输入函数"
  ([]
   (read-line))
  ([prompt]
   (print prompt)
   (flush)
   (read-line)))

(defn 长度
  "获取长度"
  [value]
  (cond
    (string? value) (count value)
    (coll? value) (count value)
    :else 0))

(defn 连接
  "连接字符串或集合"
  [& args]
  (if (every? string? args)
    (str/join args)
    (apply concat args)))

(defn 包含?
  "检查是否包含"
  [container item]
  (cond
    (string? container) (str/includes? container (str item))
    (coll? container) (some #(= % item) container)
    :else false))

(defn 为空?
  "检查是否为空"
  [value]
  (cond
    (nil? value) true
    (string? value) (empty? value)
    (coll? value) (empty? value)
    :else false))

(defn 不为空?
  "检查是否不为空"
  [value]
  (not (为空? value)))

(defn 加
  "加法运算"
  [& args]
  (apply + args))

(defn 减
  "减法运算"
  [& args]
  (apply - args))

(defn 乘
  "乘法运算"
  [& args]
  (apply * args))

(defn 除
  "除法运算"
  [& args]
  (apply / args))

(defn 等于?
  "相等比较"
  [a b]
  (= a b))

(defn 大于?
  "大于比较"
  [a b]
  (> a b))

(defn 小于?
  "小于比较"
  [a b]
  (< a b))

(defn 大于等于?
  "大于等于比较"
  [a b]
  (>= a b))

(defn 小于等于?
  "小于等于比较"
  [a b]
  (<= a b))

(def builtin-functions
  "内置函数映射表"
  {'调试输出 调试输出
   '调试输出_ 调试输出_
   '到整数 到整数
   '到字符串 到字符串
   '到浮点数 到浮点数
   '返回 返回
   '打印 打印
   '输出 输出
   '输入 输入
   '长度 长度
   '连接 连接
   '包含? 包含?
   '为空? 为空?
   '不为空? 不为空?
   '加 加
   '减 减
   '乘 乘
   '除 除
   '等于? 等于?
   '大于? 大于?
   '小于? 小于?
   '大于等于? 大于等于?
   '小于等于? 小于等于?
   ;; 别名支持
   '+ 加
   '- 减
   '* 乘
   '/ 除
   '= 等于?
   '> 大于?
   '< 小于?
   '>= 大于等于?
   '<= 小于等于?})

(defn get-builtin-function
  "获取内置函数"
  [name]
  (get builtin-functions (symbol name)))
