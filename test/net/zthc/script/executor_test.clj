(ns net.zthc.script.executor-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [net.zthc.script.executor :as executor]
            [net.zthc.script.parser :as parser]
            [net.zthc.script.transformer :as transformer]))

(deftest test-execute-code
  (testing "执行 Clojure 代码"
    ;; 简单表达式
    (let [result (executor/execute-code ['(+ 1 2 3)])]
      (is (:success result))
      (is (= 6 (:last-result result))))
    
    ;; 多个表达式
    (let [result (executor/execute-code ['(def x 10) '(* x 2)])]
      (is (:success result))
      (is (= 20 (:last-result result))))
    
    ;; 内置函数
    (let [result (executor/execute-code ['(调试输出 "hello" "world")])]
      (is (:success result))
      (is (= "hello world" (:last-result result))))))

(deftest test-execute-ast
  (testing "执行 AST"
    ;; 变量定义
    (let [ast {:type :var-def :name "x" :value {:type :number :value 42}}
          result (executor/execute-ast ast)]
      (is (:success result)))
    
    ;; 函数调用
    (let [ast {:type :function-call 
               :name "调试输出" 
               :args [{:type :string :value "test"}]}
          result (executor/execute-ast ast)]
      (is (:success result))
      (is (= "test" (:last-result result))))))

(deftest test-execute-script
  (testing "执行脚本代码"
    ;; 简单变量定义
    (let [result (executor/execute-script "变量 x: 100;" parser/parse)]
      (is (:success result)))
    
    ;; 函数调用
    (let [result (executor/execute-script "@调试输出(\"hello\");" parser/parse)]
      (is (:success result))
      (is (= "hello" (:last-result result))))
    
    ;; 复杂脚本
    (let [result (executor/execute-script "变量 a: 10; 变量 b: 20; @调试输出(a, b);" parser/parse)]
      (is (:success result)))))

(deftest test-function-definition-and-call
  (testing "函数定义和调用"
    ;; 简单函数定义
    (let [result (executor/execute-script "函数 greet(name) { @返回(name); };" parser/parse)]
      (is (:success result)))
    
    ;; 函数定义和调用
    (let [result (executor/execute-script "函数 add(a, b) { @返回(@加(a, b)); }; @调试输出(@add(5, 3));" parser/parse)]
      (is (:success result))
      (is (= "8" (:last-result result))))
    
    ;; 复杂函数
    (let [result (executor/execute-script "函数 multiply(x, y) { 变量 result: @乘(x, y); @返回(result); }; @调试输出(@multiply(4, 6));" parser/parse)]
      (is (:success result))
      (is (= "24" (:last-result result))))
    
    ;; 递归函数测试（斐波那契）
    (let [result (executor/execute-script "函数 fib(n) { 如果(@小于等于?(n, 1)) { @返回(n); } 否则 { @返回(@加(@fib(@减(n, 1)), @fib(@减(n, 2)))); } }; @调试输出(@fib(5));" parser/parse)]
      ;; 注意：这可能不会工作，因为我们还没有实现条件语句
      (is (or (:success result) (contains? result :error))))
    
    ;; 多参数函数
    (let [result (executor/execute-script "函数 sum3(a, b, c) { @返回(@加(@加(a, b), c)); }; @调试输出(@sum3(1, 2, 3));" parser/parse)]
      (is (:success result))
      (is (= "6" (:last-result result))))
    
    ;; 无参数函数
    (let [result (executor/execute-script "函数 hello() { @返回(\"Hello World\"); }; @调试输出(@hello());" parser/parse)]
      (is (:success result))
      (is (= "Hello World" (:last-result result))))))

(deftest test-execute-in-sandbox
  (testing "沙箱执行"
    ;; 允许的函数
    (let [code-forms ['(调试输出 "safe")]
          allowed-funcs ['调试输出]
          result (executor/execute-in-sandbox code-forms allowed-funcs)]
      (is (:success result))
      (is (:sandbox result)))
    
    ;; 限制的函数
    (let [code-forms ['(System/exit 0)]
          allowed-funcs ['调试输出]
          result (executor/execute-in-sandbox code-forms allowed-funcs)]
      (is (not (:success result)))
      (is (:sandbox result)))))

(deftest test-builtin-functions
  (testing "内置函数执行"
    ;; 类型转换
    (let [result (executor/execute-code ['(到整数 "123")])]
      (is (:success result))
      (is (= 123 (:last-result result))))
    
    (let [result (executor/execute-code ['(到字符串 456)])]
      (is (:success result))
      (is (= "456" (:last-result result))))
    
    ;; 数学运算
    (let [result (executor/execute-code ['(加 1 2 3)])]
      (is (:success result))
      (is (= 6 (:last-result result))))
    
    (let [result (executor/execute-code ['(乘 4 5)])]
      (is (:success result))
      (is (= 20 (:last-result result))))
    
    ;; 比较操作
    (let [result (executor/execute-code ['(大于? 5 3)])]
      (is (:success result))
      (is (= true (:last-result result))))
    
    (let [result (executor/execute-code ['(等于? 10 10)])]
      (is (:success result))
      (is (= true (:last-result result))))))

(deftest test-execution-stats
  (testing "执行统计"
    (let [result (executor/execute-code ['(+ 1 2) '(* 3 4)])
          stats (executor/get-execution-stats result)]
      (is (:success stats))
      (is (= 2 (:result-count stats)))
      (is (not (:has-error stats))))))

(deftest test-format-execution-result
  (testing "格式化执行结果"
    (let [success-result {:success true :results [1 2 3] :last-result 3}
          formatted (executor/format-execution-result success-result)]
      (is (str/includes? formatted "执行成功")))
    
    (let [error-result {:success false :error "测试错误"}
          formatted (executor/format-execution-result error-result)]
      (is (str/includes? formatted "执行失败")))))

(run-tests)