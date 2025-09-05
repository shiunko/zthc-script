(ns net.zthc.script.parser.symbol
  (:require
   [clojure.string :as str]))

(def chinese-keywords
  "中文关键字映射"
  {"函数" :function
   "变量" :variable
   "常量" :constant
   "如果" :if
   "否则" :else
   "循环" :loop
   "遍历" :foreach
   "返回" :return
   "结束" :end
   "真" :true
   "假" :false
   "空" :null
   "打印" :print
   "输出" :output
   "输入" :input
   "调试输出" :debug-output})

(def builtin-functions
  "内置函数映射"
  {"调试输出" :debug-output
   "调试输出_" :debug-output-inline
   "到整数" :to-integer
   "到字符串" :to-string
   "到浮点数" :to-float
   "返回" :return
   "打印" :print
   "输出" :output})

(defn chinese-keyword?
  "检查是否为中文关键字"
  [s]
  (contains? chinese-keywords s))

(defn builtin-function?
  "检查是否为内置函数"
  [s]
  (contains? builtin-functions s))

(defn valid-identifier?
  "检查是否为有效的标识符"
  [s]
  (and (not (empty? s))
       (re-matches #"[a-zA-Z_\u4e00-\u9fff][a-zA-Z0-9_\u4e00-\u9fff]*" s)))

(defn parse-identifier
  "解析标识符"
  [s]
  (let [s (str/trim s)]
    (cond
      ;; 内置函数
      (builtin-function? s)
      {:type :builtin-function
       :value (builtin-functions s)
       :raw s}

      ;; 中文关键字
      (chinese-keyword? s)
      {:type :keyword
       :subtype :chinese
       :value (chinese-keywords s)
       :raw s}

      ;; 普通标识符
      (valid-identifier? s)
      {:type :identifier
       :value s
       :raw s}

      :else
      {:type :identifier
       :value s
       :raw s
       :error (str "无效的标识符: " s)})))

(defn normalize-identifier
  "标准化标识符（处理中文和英文混合）"
  [s]
  (-> s
      str/trim
      (str/replace #"\s+" "_")))

(defn validate-identifier
  "验证标识符语法"
  [s]
  (let [s (str/trim s)]
    (cond
      (empty? s)
      {:valid false :error "标识符不能为空"}

      (not (valid-identifier? s))
      {:valid false :error "标识符包含无效字符"}

      ;; 不能以数字开头
      (Character/isDigit (first s))
      {:valid false :error "标识符不能以数字开头"}

      ;; 不能是纯数字
      (re-matches #"\d+" s)
      {:valid false :error "标识符不能是纯数字"}

      :else
      {:valid true})))

(defn get-keyword-type
  "获取关键字类型"
  [s]
  (cond
    (builtin-function? s) :builtin-function
    (chinese-keyword? s) :chinese-keyword
    :else :identifier))

(defn format-identifier
  "格式化标识符输出"
  [identifier]
  (if (string? identifier)
    identifier
    (str identifier)))
