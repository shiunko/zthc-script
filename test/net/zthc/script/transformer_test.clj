(ns net.zthc.script.transformer-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.transformer :as transformer]))

(deftest test-transform-expression
  (testing "转换表达式"
    ;; 字符串
    (let [expr {:type :string :value "hello"}
          result (transformer/transform-expression expr)]
      (is (= "hello" result)))
    
    ;; 数字
    (let [expr {:type :number :value 42}
          result (transformer/transform-expression expr)]
      (is (= 42 result)))
    
    ;; 布尔值
    (let [expr {:type :boolean :value true}
          result (transformer/transform-expression expr)]
      (is (= true result)))
    
    ;; 标识符
    (let [expr {:type :identifier :value "x"}
          result (transformer/transform-expression expr)]
      (is (= 'x result)))
    
    ;; 函数调用
    (let [expr {:type :function-call 
                :name "调试输出" 
                :args [{:type :string :value "test"}]}
          result (transformer/transform-expression expr)]
      (is (= '(调试输出 "test") result)))))

(deftest test-transform-var-def
  (testing "转换变量定义"
    (let [var-def {:type :var-def 
                   :name "x" 
                   :value {:type :number :value 42}}
          result (transformer/transform-var-def var-def)]
      (is (= '(def x 42) result)))))

(deftest test-transform-function-def
  (testing "转换函数定义"
    (let [func-def {:type :function-def
                    :name "add"
                    :params ["a" "b"]
                    :body [{:type :function-call
                            :name "返回"
                            :args [{:type :identifier :value "sum"}]}]}
          result (transformer/transform-function-def func-def)]
      (is (= `defn (first result)))
      (is (= 'add (second result)))
      (is (= '[a b] (nth result 2))))))

(deftest test-transform-function-call
  (testing "转换函数调用"
    (let [func-call {:type :function-call
                     :name "调试输出"
                     :args [{:type :string :value "hello"}
                            {:type :number :value 123}]}
          result (transformer/transform-function-call func-call)]
      (is (= '(调试输出 "hello" 123) result)))))

(deftest test-transform-ast
  (testing "转换 AST 节点"
    ;; 变量定义
    (let [ast {:type :var-def :name "x" :value {:type :number :value 10}}
          result (transformer/transform-ast ast)]
      (is (= '(def x 10) result)))
    
    ;; 函数调用
    (let [ast {:type :function-call 
               :name "打印" 
               :args [{:type :string :value "test"}]}
          result (transformer/transform-ast ast)]
      (is (= '(打印 "test") result)))))

(deftest test-ast-to-clojure
  (testing "AST 转换为 Clojure 代码"
    ;; 单个节点
    (let [ast {:type :var-def :name "x" :value {:type :number :value 5}}
          result (transformer/ast-to-clojure ast)]
      (is (= ['(def x 5)] result)))
    
    ;; 多个节点
    (let [ast [{:type :var-def :name "x" :value {:type :number :value 1}}
               {:type :function-call :name "调试输出" :args [{:type :identifier :value "x"}]}]
          result (transformer/ast-to-clojure ast)]
      (is (= 2 (count result)))
      (is (= '(def x 1) (first result)))
      (is (= '(调试输出 x) (second result))))))

(deftest test-prepare-execution-context
  (testing "准备执行上下文"
    (let [code-forms ['(def x 10) '(defn add [a b] (+ a b)) '(调试输出 x)]
          context (transformer/prepare-execution-context code-forms)]
      (is (= 2 (count (:definitions context))))
      (is (= 1 (count (:expressions context)))))))

(deftest test-optimize-code
  (testing "优化代码"
    (let [code-forms ['(def x 10) nil '(调试输出 x) [] '(+ 1 2)]
          optimized (transformer/optimize-code code-forms)]
      (is (= 3 (count optimized)))
      (is (not (some nil? optimized)))
      (is (not (some empty? optimized))))))

(deftest test-validate-transformation
  (testing "验证转换"
    (let [ast {:type :var-def :name "x" :value {:type :number :value 1}}
          code ['(def x 1)]
          validation (transformer/validate-transformation ast code)]
      (is (:valid validation))
      (is (= 1 (:original-count validation)))
      (is (= 1 (:transformed-count validation))))))

(deftest test-transform-return
  (testing "转换返回语句"
    ;; 有参数的返回语句
    (let [return-stmt {:type :return :value {:type :number :value 42}}
          result (transformer/transform-return return-stmt)]
      (is (= '(返回 42) result)))
    
    ;; 有参数的返回语句（字符串）
    (let [return-stmt {:type :return :value {:type :string :value "hello"}}
          result (transformer/transform-return return-stmt)]
      (is (= '(返回 "hello") result)))
    
    ;; 有参数的返回语句（函数调用）
    (let [return-stmt {:type :return 
                       :value {:type :function-call 
                               :name "加" 
                               :args [{:type :number :value 1} 
                                      {:type :number :value 2}]}}
          result (transformer/transform-return return-stmt)]
      (is (= '(返回 (加 1 2)) result)))
    
    ;; 无参数的返回语句
    (let [return-stmt {:type :return :value nil}
          result (transformer/transform-return return-stmt)]
      (is (= '(返回) result)))))

(deftest test-transform-ast-with-return
  (testing "转换包含返回语句的 AST 节点"
    ;; 返回语句
    (let [ast {:type :return :value {:type :number :value 123}}
          result (transformer/transform-ast ast)]
      (is (= '(返回 123) result)))
    
    ;; 无参数返回语句
    (let [ast {:type :return :value nil}
          result (transformer/transform-ast ast)]
      (is (= '(返回) result)))))

(deftest test-early-return-detection
  (testing "早期返回检测"
    ;; 包含早期返回的函数体
    (let [body-forms ['(调试输出 "test") '(返回 42) '(调试输出 "never reached")]]
      (is (transformer/has-early-return? body-forms)))
    
    ;; 不包含早期返回的函数体
    (let [body-forms ['(调试输出 "test") '(调试输出 "end") '(返回 42)]]
      (is (not (transformer/has-early-return? body-forms))))
    
    ;; 空函数体
    (let [body-forms []]
      (is (not (transformer/has-early-return? body-forms))))))

(deftest test-function-body-optimization
  (testing "函数体优化"
    ;; 优化普通函数体（最后一个返回语句）
    (let [body-forms ['(调试输出 "test") '(返回 42)]
          optimized (transformer/optimize-function-body body-forms)]
      (is (= ['(调试输出 "test") 42] optimized)))
    
    ;; 优化无返回语句的函数体
    (let [body-forms ['(调试输出 "test") '(调试输出 "end")]
          optimized (transformer/optimize-function-body body-forms)]
      (is (= body-forms optimized)))
    
    ;; 优化空函数体
    (let [body-forms []]
      (is (= [] (transformer/optimize-function-body body-forms))))))

(run-tests)