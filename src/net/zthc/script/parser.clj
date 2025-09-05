(ns net.zthc.script.parser
  (:require
   [clojure.string :as str]
   [net.zthc.script.parser.number :as number-parser]
   [net.zthc.script.parser.string :as string-parser]
   [net.zthc.script.parser.symbol :as symbol-parser]
   [net.zthc.script.util :as util]))

(defn parse-string
  "解析字符串文字"
  [s]
  (string-parser/parse-string s))

(defn parse-number
  "解析数字文字"
  [s]
  (number-parser/parse-number s))

(defn parse-identifier
  "解析标识符"
  [s]
  (symbol-parser/parse-identifier s))

(declare parse-expression parse-statements parse-statement)

(defn parse-function-call
  "解析函数调用"
  [call-str]
  (try
    (let [call-str (str/trim call-str)
          call-str (if (str/ends-with? call-str ";")
                     (subs call-str 0 (dec (count call-str)))
                     call-str)]

      (if (str/starts-with? call-str "@")
        (let [func-part (subs call-str 1)
              paren-start (str/index-of func-part "(")
              paren-end (str/last-index-of func-part ")")]

          (if (and paren-start paren-end (> paren-end paren-start))
            (let [func-name (str/trim (subs func-part 0 paren-start))
                  args-str (subs func-part (inc paren-start) paren-end)
                  args (if (empty? (str/trim args-str))
                         []
                         (util/split-function-args args-str))]
              {:type :function-call
               :name func-name
               :args (mapv parse-expression args)})
            (util/format-error :parse-error "无效的函数调用语法: %s" call-str)))
        (util/format-error :parse-error "函数调用必须以@开头: %s" call-str)))

    (catch Exception e
      (util/log :error "解析函数调用失败: %s, 错误: %s" call-str (.getMessage e))
      (util/format-error :parse-error "解析函数调用失败: %s" (.getMessage e)))))

(defn parse-def-var
  "解析变量定义"
  [def-str]
  (try
    (let [def-str (str/trim def-str)
          def-str (if (str/ends-with? def-str ";")
                    (subs def-str 0 (dec (count def-str)))
                    def-str)]

      (if (str/starts-with? def-str "变量")
        (let [content (str/trim (subs def-str 2))
              colon-idx (str/index-of content ":")]

          (if colon-idx
            (let [var-name (str/trim (subs content 0 colon-idx))
                  var-value (str/trim (subs content (inc colon-idx)))]
              {:type :var-def
               :name var-name
               :value (parse-expression var-value)})
            (util/format-error :parse-error "变量定义缺少冒号: %s" def-str)))
        (util/format-error :parse-error "变量定义必须以'变量'开头: %s" def-str)))

    (catch Exception e
      (util/log :error "解析变量定义失败: %s, 错误: %s" def-str (.getMessage e))
      (util/format-error :parse-error "解析变量定义失败: %s" (.getMessage e)))))

(defn parse-def-function
  "解析函数定义"
  [def-str]
  (try
    (let [def-str (str/trim def-str)]

      (if (str/starts-with? def-str "函数")
        (let [content (str/trim (subs def-str 2))
              paren-start (str/index-of content "(")
              paren-end (str/index-of content ")")
              brace-start (str/index-of content "{")
              brace-end (str/last-index-of content "}")]

          (if (and paren-start paren-end brace-start brace-end
                   (< paren-start paren-end)
                   (< paren-end brace-start)
                   (< brace-start brace-end))
            (let [func-name (str/trim (subs content 0 paren-start))
                  params-str (subs content (inc paren-start) paren-end)
                  body-str (subs content (inc brace-start) brace-end)
                  params (if (empty? (str/trim params-str))
                           []
                           (mapv str/trim (str/split params-str #",")))]
              {:type :function-def
               :name func-name
               :params params
               :body (parse-statements body-str)})
            (util/format-error :parse-error "无效的函数定义语法: %s" def-str)))
        (util/format-error :parse-error "函数定义必须以'函数'开头: %s" def-str)))

    (catch Exception e
      (util/log :error "解析函数定义失败: %s, 错误: %s" def-str (.getMessage e))
      (util/format-error :parse-error "解析函数定义失败: %s" (.getMessage e)))))

(defn parse-expression
  "解析表达式"
  [expr-str]
  (let [expr-str (str/trim expr-str)]
    (cond
      ;; 函数调用
      (str/starts-with? expr-str "@")
      (parse-function-call expr-str)

      ;; 字符串
      (or (and (str/starts-with? expr-str "\"") (str/ends-with? expr-str "\""))
          (and (str/starts-with? expr-str "'") (str/ends-with? expr-str "'"))
          (and (str/starts-with? expr-str "「") (str/ends-with? expr-str "」"))
          (and (str/starts-with? expr-str "\\") (str/ends-with? expr-str "\\")))
      (parse-string expr-str)

      ;; 数字
      (re-matches #"-?\d+(\.\d+)?" expr-str)
      (parse-number expr-str)

      ;; 标识符
      :else
      (parse-identifier expr-str))))

(defn parse-statements
  "解析多个语句"
  [code]
  (let [statements (-> code
                       (str/replace #"\n" " ")
                       (str/split #";"))]
    (->> statements
         (map str/trim)
         (filter #(not (empty? %)))
         (mapv parse-statement))))

(defn parse-statement
  "解析单个语句"
  [stmt-str]
  (let [stmt-str (str/trim stmt-str)]
    (cond
      (str/starts-with? stmt-str "函数")
      (parse-def-function stmt-str)

      (str/starts-with? stmt-str "变量")
      (parse-def-var stmt-str)

      (str/starts-with? stmt-str "常量")
      (parse-def-var (str/replace-first stmt-str "常量" "变量"))

      (str/starts-with? stmt-str "@")
      (parse-function-call stmt-str)

      :else
      (parse-expression stmt-str))))

(defn parse
  "主解析函数"
  [code]
  (try
    (util/log :info "解析代码: %s" code)
    (parse-statements code)
    (catch Exception e
      (util/log :error "解析代码失败: %s" (.getMessage e))
      (util/format-error :parse-error "解析失败: %s" (.getMessage e)))))

(defn parse-and-execute
  "解析并准备执行代码"
  [code]
  (let [ast (parse code)]
    (if (and (map? ast) (:error-type ast))
      ast  ; 返回解析错误
      {:parsed-ast ast
       :ready-for-execution true
       :original-code code})))
