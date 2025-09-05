(ns net.zthc.script.parser-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.parser :as parser]))

(deftest test-parse-function-call
  (testing "函数调用解析"
    (let [result (parser/parse-function-call "@调试输出(\"hello\", 123);")]
      (is (= :function-call (:type result)))
      (is (= "调试输出" (:name result)))
      (is (= 2 (count (:args result)))))
    
    (let [result (parser/parse-function-call "@test();")]
      (is (= :function-call (:type result)))
      (is (= "test" (:name result)))
      (is (= 0 (count (:args result)))))
    
    (let [result (parser/parse-function-call "invalid")]
      (is (contains? result :error-type)))))

(deftest test-parse-def-var
  (testing "变量定义解析"
    (let [result (parser/parse-def-var "变量 a: 123;")]
      (is (= :var-def (:type result)))
      (is (= "a" (:name result))))
    
    (let [result (parser/parse-def-var "变量 name: \"hello\";")]
      (is (= :var-def (:type result)))
      (is (= "name" (:name result))))
    
    (let [result (parser/parse-def-var "invalid syntax")]
      (is (contains? result :error-type)))))

(deftest test-parse-def-function
  (testing "函数定义解析"
    (let [result (parser/parse-def-function "函数 add(a, b) { @返回(a+b) }")]
      (is (= :function-def (:type result)))
      (is (= "add" (:name result)))
      (is (= ["a" "b"] (:params result))))
    
    (let [result (parser/parse-def-function "函数 test() { @调试输出(\"test\") }")]
      (is (= :function-def (:type result)))
      (is (= "test" (:name result)))
      (is (= [] (:params result))))
    
    ;; 测试复杂函数定义
    (let [result (parser/parse-def-function "函数 multiply(x, y) { @返回(@乘(x, y)); }")]
      (is (= :function-def (:type result)))
      (is (= "multiply" (:name result)))
      (is (= ["x" "y"] (:params result)))
      (is (= 1 (count (:body result)))))
    
    ;; 测试多语句函数体
    (let [result (parser/parse-def-function "函数 greet(name) { @调试输出(\"Hello\", name); @返回(name); }")]
      (is (= :function-def (:type result)))
      (is (= "greet" (:name result)))
      (is (= ["name"] (:params result)))
      (is (= 2 (count (:body result)))))
    
    (let [result (parser/parse-def-function "invalid function")]
      (is (contains? result :error-type)))))

(deftest test-parse-expression
  (testing "表达式解析"
    ;; 字符串表达式
    (let [result (parser/parse-expression "\"hello\"")]
      (is (= :string (:type result))))
    
    ;; 数字表达式
    (let [result (parser/parse-expression "123")]
      (is (= :number (:type result))))
    
    ;; 函数调用表达式
    (let [result (parser/parse-expression "@test()")]
      (is (= :function-call (:type result))))
    
    ;; 标识符表达式
    (let [result (parser/parse-expression "variable")]
      (is (= :identifier (:type result))))))

(deftest test-parse-statements
  (testing "多语句解析"
    (let [code "变量 a: 123; @调试输出(a);"
          results (parser/parse-statements code)]
      (is (= 2 (count results)))
      (is (= :var-def (:type (first results))))
      (is (= :function-call (:type (second results)))))
    
    ;; 测试函数定义和调用
    (let [code "函数 add(a, b) { @返回(@加(a, b)); }; @调试输出(@add(10, 20));"
          results (parser/parse-statements code)]
      (is (= 2 (count results)))
      (is (= :function-def (:type (first results))))
      (is (= :function-call (:type (second results))))
      (is (= "add" (:name (first results))))
      (is (= "调试输出" (:name (second results)))))
    
    (let [results (parser/parse-statements "")]
      (is (= 0 (count results))))))

(deftest test-parse-statement
  (testing "单语句解析"
    ;; 函数定义
    (let [result (parser/parse-statement "函数 test() { }")]
      (is (= :function-def (:type result))))
    
    ;; 变量定义
    (let [result (parser/parse-statement "变量 x: 1")]
      (is (= :var-def (:type result))))
    
    ;; 常量定义
    (let [result (parser/parse-statement "常量 PI: 3.14")]
      (is (= :var-def (:type result))))
    
    ;; 函数调用
    (let [result (parser/parse-statement "@调试输出(\"test\")")]
      (is (= :function-call (:type result))))))

(run-tests)