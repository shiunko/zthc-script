(ns net.zthc.script.executor
  (:require
    [net.zthc.script.builtins :as builtins]
    [net.zthc.script.transformer :as transformer]
    [net.zthc.script.util :as util]
    [sci.core :as sci]))

(def default-context
  "默认执行上下文"
  {:namespaces {'net.zthc.script.early-return (update-vals (ns-publics 'net.zthc.script.early-return)
                                                           deref)
                'user                         builtins/builtin-functions
                'clojure.core                 {'+      +
                                               '-      -
                                               '*      *
                                               '/      /
                                               '=      =
                                               '>      >
                                               '<      <
                                               '>=     >=
                                               '<=     <=
                                               'str    str
                                               'count  count
                                               'empty? empty?
                                               'nil?   nil?
                                               'not    not
                                               'and    (fn [& args] (every? identity args))
                                               'or     (fn [& args] (some identity args))
                                               'if     (fn [condition then-branch else-branch]
                                                         (if condition then-branch else-branch))
                                               'when   (fn [condition & body]
                                                         (when condition (last body)))
                                               'cond   (fn [& clauses]
                                                         (loop [clauses clauses]
                                                           (when (seq clauses)
                                                             (let [condition (first clauses)
                                                                   result (second clauses)]
                                                               (if (or (= condition :else) condition)
                                                                 result
                                                                 (recur (drop 2 clauses)))))))
                                               'do     (fn [& body] (last body))
                                               'let    (fn [bindings & body]
                                                         (let [vars (take-nth 2 bindings)
                                                               vals (take-nth 2 (rest bindings))]
                                                           (last body)))}}
   :classes    {'java.lang.String  String
                'java.lang.Integer Integer
                'java.lang.Long    Long
                'java.lang.Double  Double}})

(defn create-execution-context
  "创建执行上下文"
  [additional-bindings]
  (let [base-bindings (merge builtins/builtin-functions additional-bindings)]
    (sci/init
      (update-in default-context [:namespace 'user] (partial merge base-bindings)))))

(defn execute-code
  "执行 Clojure 代码"
  ([code-forms]
   (execute-code code-forms {}))
  ([code-forms bindings]
   (try
     (let [ctx (create-execution-context bindings)
           results (atom [])]

       (doseq [form code-forms]
         (when form
           (let [result (sci/eval-form ctx form)]
             (swap! results conj result))))

       {:success         true
        :results         @results
        :last-result     (last @results)
        :execution-count (count @results)})

     (catch Exception e
       (util/log :error "代码执行失败: %s" (.getMessage e))
       {:success   false
        :error     (.getMessage e)
        :message   "代码执行失败"
        :exception e}))))

(defn execute-ast
  "执行 AST"
  ([ast]
   (execute-ast ast {}))
  ([ast bindings]
   (try
     (util/log :info "开始执行 AST")

     ;; 转换 AST 为 Clojure 代码
     (let [code-forms (transformer/ast-to-clojure ast)
           optimized-code (transformer/optimize-code code-forms)]

       (util/log :debug "转换后的代码: %s" (transformer/format-code-for-display optimized-code))

       ;; 验证转换
       (let [validation (transformer/validate-transformation ast optimized-code)]
         (if (:valid validation)
           ;; 执行代码
           (let [execution-result (execute-code optimized-code bindings)]
             (merge execution-result
                    {:transformed-code optimized-code
                     :validation       validation}))

           ;; 转换验证失败
           {:success    false
            :error      "AST 转换失败"
            :validation validation})))

     (catch Exception e
       (util/log :error "AST 执行失败: %s" (.getMessage e))
       {:success   false
        :error     (.getMessage e)
        :message   "AST 执行失败"
        :exception e}))))

(defn execute-script
  "执行脚本代码（从字符串到执行）"
  ([script-code parser-fn]
   (execute-script script-code parser-fn {}))
  ([script-code parser-fn bindings]
   (try
     (util/log :info "开始执行脚本: %s" script-code)

     ;; 解析脚本
     (let [parse-result (parser-fn script-code)]
       (if (and (map? parse-result) (:error-type parse-result))
         ;; 解析失败
         {:success     false
          :error       "脚本解析失败"
          :parse-error parse-result}

         ;; 解析成功，执行 AST
         (let [execution-result (execute-ast parse-result bindings)]
           (merge execution-result
                  {:original-script script-code
                   :parsed-ast      parse-result}))))

     (catch Exception e
       (util/log :error "脚本执行失败: %s" (.getMessage e))
       {:success   false
        :error     (.getMessage e)
        :message   "脚本执行失败"
        :exception e}))))

(defn create-sandbox-context
  "创建沙箱执行上下文（限制权限）"
  [allowed-functions]
  (let [safe-bindings (select-keys builtins/builtin-functions allowed-functions)]
    (sci/init {:namespaces {'user safe-bindings}
               :deny       '[java.lang.System/exit
                             java.lang.Runtime/exec
                             java.io.File
                             java.nio.file.Files]})))

(defn execute-in-sandbox
  "在沙箱中执行代码"
  [code-forms allowed-functions]
  (try
    (let [ctx (create-sandbox-context allowed-functions)
          results (atom [])]

      (doseq [form code-forms]
        (when form
          (let [result (sci/eval-form ctx form)]
            (swap! results conj result))))

      {:success     true
       :results     @results
       :last-result (last @results)
       :sandbox     true})

    (catch Exception e
      (util/log :error "沙箱执行失败: %s" (.getMessage e))
      {:success false
       :error   (.getMessage e)
       :message "沙箱执行失败"
       :sandbox true})))

(defn get-execution-stats
  "获取执行统计信息"
  [execution-result]
  {:success        (:success execution-result)
   :execution-time (get execution-result :execution-time 0)
   :result-count   (count (get execution-result :results []))
   :has-error      (contains? execution-result :error)
   :sandbox-mode   (get execution-result :sandbox false)})

(defn format-execution-result
  "格式化执行结果用于显示"
  [execution-result]
  (if (:success execution-result)
    (str "执行成功\n"
         "结果数量: " (count (get execution-result :results [])) "\n"
         "最后结果: " (:last-result execution-result))
    (str "执行失败\n"
         "错误: " (:error execution-result))))

(defn clear-execution-context
  "清理执行上下文"
  []
  (System/gc))
