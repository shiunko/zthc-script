(ns net.zthc.script.builtins-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.builtins :as builtins]
            [clojure.string :as str]))

(deftest test-type-conversion
  (testing "类型转换函数"
    ;; 到整数
    (is (= 123 (builtins/到整数 "123")))
    (is (= 42 (builtins/到整数 42)))
    (is (= 3 (builtins/到整数 3.14)))
    (is (= 0 (builtins/到整数 "invalid")))
    
    ;; 到字符串
    (is (= "123" (builtins/到字符串 123)))
    (is (= "3.14" (builtins/到字符串 3.14)))
    (is (= "true" (builtins/到字符串 true)))
    
    ;; 到浮点数
    (is (= 123.0 (builtins/到浮点数 123)))
    (is (= 3.14 (builtins/到浮点数 "3.14")))
    (is (= 0.0 (builtins/到浮点数 "invalid")))))

(deftest test-output-functions
  (testing "输出函数"
    ;; 调试输出
    (let [result (with-out-str (builtins/调试输出 "hello" "world"))]
      (is (str/includes? result "hello world")))
    
    ;; 打印
    (let [result (with-out-str (builtins/打印 "test" 123))]
      (is (str/includes? result "test 123")))
    
    ;; 输出（不换行）
    (let [result (with-out-str (builtins/输出 "no" "newline"))]
      (is (str/includes? result "no newline"))
      (is (not (str/includes? result "\n"))))))

(deftest test-string-functions
  (testing "字符串函数"
    ;; 长度
    (is (= 5 (builtins/长度 "hello")))
    (is (= 3 (builtins/长度 [1 2 3])))
    (is (= 0 (builtins/长度 123)))
    
    ;; 连接
    (is (= "helloworld" (builtins/连接 "hello" "world")))
    (is (= [1 2 3 4] (builtins/连接 [1 2] [3 4])))
    
    ;; 包含?
    (is (builtins/包含? "hello" "ell"))
    (is (builtins/包含? [1 2 3] 2))
    (is (not (builtins/包含? "hello" "xyz")))
    
    ;; 为空?
    (is (builtins/为空? ""))
    (is (builtins/为空? []))
    (is (builtins/为空? nil))
    (is (not (builtins/为空? "hello")))
    
    ;; 不为空?
    (is (builtins/不为空? "hello"))
    (is (not (builtins/不为空? "")))))

(deftest test-math-functions
  (testing "数学函数"
    ;; 加法
    (is (= 6 (builtins/加 1 2 3)))
    (is (= 5 (builtins/加 2 3)))
    
    ;; 减法
    (is (= 5 (builtins/减 10 5)))
    (is (= -2 (builtins/减 3 5)))
    
    ;; 乘法
    (is (= 20 (builtins/乘 4 5)))
    (is (= 24 (builtins/乘 2 3 4)))
    
    ;; 除法
    (is (= 2 (builtins/除 10 5)))
    (is (= 5/2 (builtins/除 5 2)))))

(deftest test-comparison-functions
  (testing "比较函数"
    ;; 等于?
    (is (builtins/等于? 5 5))
    (is (not (builtins/等于? 5 3)))
    
    ;; 大于?
    (is (builtins/大于? 5 3))
    (is (not (builtins/大于? 3 5)))
    
    ;; 小于?
    (is (builtins/小于? 3 5))
    (is (not (builtins/小于? 5 3)))
    
    ;; 大于等于?
    (is (builtins/大于等于? 5 5))
    (is (builtins/大于等于? 5 3))
    (is (not (builtins/大于等于? 3 5)))
    
    ;; 小于等于?
    (is (builtins/小于等于? 3 3))
    (is (builtins/小于等于? 3 5))
    (is (not (builtins/小于等于? 5 3)))))

(deftest test-get-builtin-function
  (testing "获取内置函数"
    (is (= builtins/调试输出 (builtins/get-builtin-function "调试输出")))
    (is (= builtins/到整数 (builtins/get-builtin-function "到整数")))
    (is (= builtins/加 (builtins/get-builtin-function "加")))
    
    ;; 别名测试
    (is (= builtins/加 (builtins/get-builtin-function "+")))
    (is (= builtins/等于? (builtins/get-builtin-function "=")))
    
    ;; 不存在的函数
    (is (nil? (builtins/get-builtin-function "不存在的函数")))))

(deftest test-返回-function
  (testing "返回函数"
    (is (= 42 (builtins/返回 42)))
    (is (= "hello" (builtins/返回 "hello")))
    (is (= [1 2 3] (builtins/返回 [1 2 3])))))

(run-tests)