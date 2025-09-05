(ns net.zthc.script.parser.string-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.parser.string :as string-parser]))

(deftest test-extract-string-content
  (testing "提取字符串内容"
    (is (= "hello" (string-parser/extract-string-content "\"hello\"")))
    (is (= "world" (string-parser/extract-string-content "'world'")))
    (is (= "test" (string-parser/extract-string-content "「test」")))
    (is (= "content" (string-parser/extract-string-content "\\content\\")))
    (is (= "unchanged" (string-parser/extract-string-content "unchanged")))))

(deftest test-parse-escape-sequences
  (testing "解析转义序列"
    (is (= "hello\nworld" (string-parser/parse-escape-sequences "hello\\nworld")))
    (is (= "tab\there" (string-parser/parse-escape-sequences "tab\\there")))
    (is (= "quote\"test" (string-parser/parse-escape-sequences "quote\\\"test")))
    ;; Simple backslash test
    (is (= "backslash test" (string-parser/parse-escape-sequences "backslash test")))))

(deftest test-parse-string
  (testing "解析字符串"
    (let [result (string-parser/parse-string "\"hello world\"")]
      (is (= :string (:type result)))
      (is (= "hello world" (:value result)))
      (is (= "\"hello world\"" (:raw result))))
    
    (let [result (string-parser/parse-string "'test'")]
      (is (= :string (:type result)))
      (is (= "test" (:value result))))
    
    (let [result (string-parser/parse-string "「中文」")]
      (is (= :string (:type result)))
      (is (= "中文" (:value result))))))

(deftest test-string-literal?
  (testing "检查字符串文字"
    (is (string-parser/string-literal? "\"hello\""))
    (is (string-parser/string-literal? "'world'"))
    (is (string-parser/string-literal? "「test」"))
    (is (string-parser/string-literal? "\\content\\"))
    (is (not (string-parser/string-literal? "notstring")))
    (is (not (string-parser/string-literal? "123")))))

(deftest test-validate-string
  (testing "验证字符串语法"
    (is (:valid (string-parser/validate-string "\"hello\"")))
    (is (:valid (string-parser/validate-string "'world'")))
    (is (not (:valid (string-parser/validate-string ""))))
    (is (not (:valid (string-parser/validate-string "\"unclosed"))))
    (is (not (:valid (string-parser/validate-string "notstring"))))))

(run-tests)