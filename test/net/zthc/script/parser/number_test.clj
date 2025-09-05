(ns net.zthc.script.parser.number-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.parser.number :as number-parser]))

(deftest test-parse-integer
  (testing "解析整数"
    (let [result (number-parser/parse-integer "123")]
      (is (= :number (:type result)))
      (is (= :integer (:subtype result)))
      (is (= 123 (:value result))))
    
    (let [result (number-parser/parse-integer "-456")]
      (is (= :number (:type result)))
      (is (= :integer (:subtype result)))
      (is (= -456 (:value result))))
    
    (let [result (number-parser/parse-integer "0")]
      (is (= :number (:type result)))
      (is (= 0 (:value result))))))

(deftest test-parse-float
  (testing "解析浮点数"
    (let [result (number-parser/parse-float "3.14")]
      (is (= :number (:type result)))
      (is (= :float (:subtype result)))
      (is (= 3.14 (:value result))))
    
    (let [result (number-parser/parse-float "-2.5")]
      (is (= :number (:type result)))
      (is (= :float (:subtype result)))
      (is (= -2.5 (:value result))))
    
    (let [result (number-parser/parse-float "0.0")]
      (is (= :number (:type result)))
      (is (= 0.0 (:value result))))))

(deftest test-parse-number
  (testing "解析数字"
    ;; 整数
    (let [result (number-parser/parse-number "42")]
      (is (= :integer (:subtype result)))
      (is (= 42 (:value result))))
    
    ;; 浮点数
    (let [result (number-parser/parse-number "3.14159")]
      (is (= :float (:subtype result)))
      (is (= 3.14159 (:value result))))
    
    ;; 科学计数法
    (let [result (number-parser/parse-number "1.23e4")]
      (is (= :float (:subtype result)))
      (is (= 12300.0 (:value result))))
    
    ;; 无效数字
    (let [result (number-parser/parse-number "abc")]
      (is (= :invalid (:subtype result)))
      (is (contains? result :error)))))

(deftest test-number-literal?
  (testing "检查数字文字"
    (is (number-parser/number-literal? "123"))
    (is (number-parser/number-literal? "-456"))
    (is (number-parser/number-literal? "3.14"))
    (is (number-parser/number-literal? "-2.5"))
    (is (number-parser/number-literal? "1.23e4"))
    (is (number-parser/number-literal? "1E-5"))
    (is (not (number-parser/number-literal? "abc")))
    (is (not (number-parser/number-literal? "12.34.56")))))

(deftest test-validate-number
  (testing "验证数字语法"
    (is (:valid (number-parser/validate-number "123")))
    (is (:valid (number-parser/validate-number "3.14")))
    (is (:valid (number-parser/validate-number "-42")))
    (is (not (:valid (number-parser/validate-number ""))))
    (is (not (:valid (number-parser/validate-number "12.34.56"))))
    (is (not (:valid (number-parser/validate-number ".123"))))
    (is (not (:valid (number-parser/validate-number "123."))))))

(deftest test-format-number
  (testing "格式化数字输出"
    (is (= "123" (number-parser/format-number 123)))
    (is (= "3.14" (number-parser/format-number 3.14)))
    (is (= "42" (number-parser/format-number 42.0)))))

(run-tests)