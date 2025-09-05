(ns net.zthc.script.parser.symbol-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.parser.symbol :as symbol-parser]))

(deftest test-chinese-keyword?
  (testing "检查中文关键字"
    (is (symbol-parser/chinese-keyword? "函数"))
    (is (symbol-parser/chinese-keyword? "变量"))
    (is (symbol-parser/chinese-keyword? "常量"))
    (is (symbol-parser/chinese-keyword? "如果"))
    (is (symbol-parser/chinese-keyword? "否则"))
    (is (symbol-parser/chinese-keyword? "循环"))
    (is (symbol-parser/chinese-keyword? "返回"))
    (is (not (symbol-parser/chinese-keyword? "hello")))
    (is (not (symbol-parser/chinese-keyword? "123")))))

(deftest test-builtin-function?
  (testing "检查内置函数"
    (is (symbol-parser/builtin-function? "调试输出"))
    (is (symbol-parser/builtin-function? "到整数"))
    (is (symbol-parser/builtin-function? "到字符串"))
    (is (symbol-parser/builtin-function? "返回"))
    (is (not (symbol-parser/builtin-function? "customfunc")))
    (is (not (symbol-parser/builtin-function? "123")))))

(deftest test-valid-identifier?
  (testing "检查有效标识符"
    (is (symbol-parser/valid-identifier? "test"))
    (is (symbol-parser/valid-identifier? "variable1"))
    (is (symbol-parser/valid-identifier? "_private"))
    (is (symbol-parser/valid-identifier? "变量名"))
    (is (symbol-parser/valid-identifier? "func_name"))
    (is (not (symbol-parser/valid-identifier? "123invalid")))
    (is (not (symbol-parser/valid-identifier? "")))
    (is (not (symbol-parser/valid-identifier? "with-dash")))))

(deftest test-parse-identifier
  (testing "解析标识符"
    ;; 中文关键字
    (let [result (symbol-parser/parse-identifier "函数")]
      (is (= :keyword (:type result)))
      (is (= :chinese (:subtype result)))
      (is (= :function (:value result))))
    
    ;; 内置函数
    (let [result (symbol-parser/parse-identifier "调试输出")]
      (is (= :builtin-function (:type result)))
      (is (= :debug-output (:value result))))
    
    ;; 普通标识符
    (let [result (symbol-parser/parse-identifier "myVariable")]
      (is (= :identifier (:type result)))
      (is (= "myVariable" (:value result))))
    
    ;; 中文标识符
    (let [result (symbol-parser/parse-identifier "我的变量")]
      (is (= :identifier (:type result)))
      (is (= "我的变量" (:value result))))))

(deftest test-validate-identifier
  (testing "验证标识符语法"
    (is (:valid (symbol-parser/validate-identifier "test")))
    (is (:valid (symbol-parser/validate-identifier "变量名")))
    (is (:valid (symbol-parser/validate-identifier "_private")))
    (is (not (:valid (symbol-parser/validate-identifier ""))))
    (is (not (:valid (symbol-parser/validate-identifier "123invalid"))))
    (is (not (:valid (symbol-parser/validate-identifier "123"))))))

(deftest test-normalize-identifier
  (testing "标准化标识符"
    (is (= "test_name" (symbol-parser/normalize-identifier "test name")))
    (is (= "my_var" (symbol-parser/normalize-identifier "  my   var  ")))
    (is (= "single" (symbol-parser/normalize-identifier "single")))))

(deftest test-get-keyword-type
  (testing "获取关键字类型"
    (is (= :chinese-keyword (symbol-parser/get-keyword-type "函数")))
    (is (= :builtin-function (symbol-parser/get-keyword-type "调试输出")))
    (is (= :identifier (symbol-parser/get-keyword-type "myvar")))))

(run-tests)