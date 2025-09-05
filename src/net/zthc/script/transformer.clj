(ns net.zthc.script.transformer
  (:require
    [clojure.string :as str]
    [net.zthc.script.early-return :as ret]))

(declare transform-ast)

(defn transform-expression
  "转换表达式 AST 为 Clojure 代码"
  [expr]
  (cond
    ;; 字符串
    (and (map? expr) (= (:type expr) :string))
    (:value expr)

    ;; 数字
    (and (map? expr) (= (:type expr) :number))
    (:value expr)

    ;; 布尔值
    (and (map? expr) (= (:type expr) :boolean))
    (:value expr)

    ;; 标识符/变量
    (and (map? expr) (= (:type expr) :identifier))
    (symbol (:value expr))

    ;; 函数调用
    (and (map? expr) (= (:type expr) :function-call))
    (let [func-name (symbol (:name expr))
          args (mapv transform-expression (:args expr))]
      (cons func-name args))

    ;; 关键字
    (and (map? expr) (= (:type expr) :keyword))
    (:value expr)

    ;; 内置函数
    (and (map? expr) (= (:type expr) :builtin-function))
    (symbol (:raw expr))

    ;; 原始值
    :else expr))

(defn transform-var-def
  "转换变量定义"
  [var-def]
  (let [var-name (symbol (:name var-def))
        var-value (transform-expression (:value var-def))]
    `(def ~var-name ~var-value)))

(defn optimize-function-body
  "优化函数体，处理返回语句"
  [body-forms]
  (if (empty? body-forms)
    []
    (let [last-form (last body-forms)
          other-forms (butlast body-forms)]
      ;; 如果最后一个表达式是 (返回 value)，提取其参数
      (if (and (seq? last-form)
               (= (first last-form) '返回)
               (= (count last-form) 2))
        (concat other-forms [(second last-form)])
        body-forms))))

(defn has-early-return?
  "检查函数体是否包含早期返回（非最后位置的返回语句）"
  [body-forms]
  (let [non-last-forms (butlast body-forms)]
    (some (fn [form]
            (and (seq? form) (= (first form) '返回)))
          non-last-forms)))

(defn transform-function-body-with-early-return
  "转换包含早期返回的函数体"
  [body-forms]
  (letfn [(transform-form [form]
            (if (and (seq? form) (= (first form) '返回))
              (if (= (count form) 1)
                `(throw (ret/->EarlyReturn nil))
                `(throw (ret/->EarlyReturn ~(second form))))
              form))]
    (map transform-form body-forms)))

(defn transform-function-def
  "转换函数定义"
  [func-def]
  (let [func-name (symbol (:name func-def))
        params (mapv symbol (:params func-def))
        raw-body (mapv transform-ast (:body func-def))]

    (if (has-early-return? raw-body)
      ;; 包含早期返回，使用异常机制
      (let [early-return-body (transform-function-body-with-early-return raw-body)]
        `(defn ~func-name ~params
           (try
             ~@early-return-body
             (catch clojure.lang.ExceptionInfo e#
               (if (ret/early-return? e#)
                 (:value e#)
                 (throw e#))))))
      ;; 普通函数，使用优化的函数体
      (let [optimized-body (optimize-function-body raw-body)]
        `(defn ~func-name ~params
           ~@optimized-body)))))

(defn transform-return
  "转换返回语句"
  [return-stmt]
  (let [value (:value return-stmt)]
    (if (nil? value)
      (list '返回)
      (list '返回 (transform-expression value)))))

(defn transform-function-call
  "转换函数调用"
  [func-call]
  (let [func-name (symbol (:name func-call))
        args (mapv transform-expression (:args func-call))]
    (cons func-name args)))

(defn transform-ast
  "转换 AST 节点为 Clojure 代码"
  [ast]
  (cond
    ;; 变量定义
    (and (map? ast) (= (:type ast) :var-def))
    (transform-var-def ast)

    ;; 函数定义
    (and (map? ast) (= (:type ast) :function-def))
    (transform-function-def ast)

    ;; 返回语句
    (and (map? ast) (= (:type ast) :return))
    (transform-return ast)

    ;; 函数调用
    (and (map? ast) (= (:type ast) :function-call))
    (transform-function-call ast)

    ;; 表达式
    (map? ast)
    (transform-expression ast)

    ;; 列表（多个语句）
    (vector? ast)
    (mapv transform-ast ast)

    ;; 其他情况直接返回
    :else ast))

(defn ast-to-clojure
  "将完整的 AST 转换为 Clojure 代码"
  [ast-nodes]
  (cond
    ;; 单个节点
    (map? ast-nodes)
    [(transform-ast ast-nodes)]

    ;; 多个节点
    (vector? ast-nodes)
    (mapv transform-ast ast-nodes)

    ;; 列表
    (seq? ast-nodes)
    (map transform-ast ast-nodes)

    ;; 其他
    :else [ast-nodes]))

(defn prepare-execution-context
  "准备执行上下文"
  [code-forms]
  (let [definitions (filter #(and (seq? %)
                                  (or (= (first %) 'def)
                                      (= (first %) 'defn))) code-forms)
        expressions (filter #(not (and (seq? %)
                                       (or (= (first %) 'def)
                                           (= (first %) 'defn)))) code-forms)]
    {:definitions definitions
     :expressions expressions}))

(defn optimize-code
  "优化生成的代码"
  [code-forms]
  (->> code-forms
       (remove nil?)
       (remove (fn [form]
                 (cond
                   ;; 对于集合类型，检查是否为空
                   (coll? form) (empty? form)
                   ;; 对于字符串，检查是否为空或仅包含空白
                   (string? form) (str/blank? form)
                   ;; 其他类型不视为"空"
                   :else false)))
       vec))

(defn format-code-for-display
  "格式化代码用于显示"
  [code-forms]
  (->> code-forms
       (map pr-str)
       (str/join "\n")))

(defn validate-transformation
  "验证转换结果"
  [original-ast transformed-code]
  (try
    {:valid             true
     :original-count    (if (vector? original-ast) (count original-ast) 1)
     :transformed-count (count transformed-code)
     :message           "转换成功"}
    (catch Exception e
      {:valid   false
       :error   (.getMessage e)
       :message "转换验证失败"})))
