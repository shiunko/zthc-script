(ns net.zthc.script.parser.atom-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.parser.atom :as atom-parser]))

(deftest test-parse-boolean
  (testing "解析布尔值"
    (let [result (atom-parser/parse-boolean "true")]
      (is (= :boolean (:type result)))
      (is (= true (:value result))))
    
    (let [result (atom-parser/parse-boolean "false")]
      (is (= :boolean (:type result)))
      (is (= false (:value result))))
    
    (let [result (atom-parser/parse-boolean "真")]
      (is (= :boolean (:type result)))
      (is (= true (:value result))))
    
    (let [result (atom-parser/parse-boolean "假")]
      (is (= :boolean (:type result)))
      (is (= false (:value result))))
    
    (let [result (atom-parser/parse-boolean "notbool")]
      (is (= :identifier (:type result)))
      (is (= "notbool" (:value result))))))

(deftest test-parse-atom
  (testing "解析原子表达式"
    ;; 布尔值
    (let [result (atom-parser/parse-atom "true")]
      (is (= :boolean (:type result)))
      (is (= true (:value result))))
    
    ;; 数字
    (let [result (atom-parser/parse-atom "123")]
      (is (= :number (:type result)))
      (is (= 123 (:value result))))
    
    (let [result (atom-parser/parse-atom "3.14")]
      (is (= :number (:type result)))
      (is (= 3.14 (:value result))))
    
    ;; 字符串
    (let [result (atom-parser/parse-atom "\"hello\"")]
      (is (= :string (:type result)))
      (is (= "hello" (:value result))))
    
    (let [result (atom-parser/parse-atom "'world'")]
      (is (= :string (:type result)))
      (is (= "world" (:value result))))
    
    (let [result (atom-parser/parse-atom "「test」")]
      (is (= :string (:type result)))
      (is (= "test" (:value result))))
    
    ;; 标识符
    (let [result (atom-parser/parse-atom "variable")]
      (is (= :identifier (:type result)))
      (is (= "variable" (:value result))))))

(deftest test-atom?
  (testing "检查是否为原子值"
    ;; 布尔值
    (is (atom-parser/atom? "true"))
    (is (atom-parser/atom? "false"))
    (is (atom-parser/atom? "真"))
    (is (atom-parser/atom? "假"))
    
    ;; 数字
    (is (atom-parser/atom? "123"))
    (is (atom-parser/atom? "-456"))
    (is (atom-parser/atom? "3.14"))
    (is (atom-parser/atom? "-2.5"))
    
    ;; 字符串
    (is (atom-parser/atom? "\"hello\""))
    (is (atom-parser/atom? "'world'"))
    (is (atom-parser/atom? "「test」"))
    (is (atom-parser/atom? "\\content\\"))
    
    ;; 非原子值
    (is (not (atom-parser/atom? "identifier")))
    (is (not (atom-parser/atom? "@function()")))))

(run-tests)