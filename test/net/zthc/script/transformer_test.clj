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

(run-tests)