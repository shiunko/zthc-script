(ns net.zthc.script.util-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.util :as util]))

(deftest test-big-first
  (testing "首字母大写功能"
    (is (= "Hello" (util/big-first "hello")))
    (is (= "World" (util/big-first "WORLD")))
    (is (= "" (util/big-first "")))
    (is (= "A" (util/big-first "a")))))

(deftest test-remove-quotes
  (testing "移除引号功能"
    (is (= "hello" (util/remove-quotes "\"hello\"")))
    (is (= "world" (util/remove-quotes "'world'")))
    (is (= "test" (util/remove-quotes "「test」")))
    (is (= "content" (util/remove-quotes "\\content\\")))
    (is (= "nochange" (util/remove-quotes "nochange")))))

(deftest test-parse-number
  (testing "数字解析功能"
    (is (= 123 (util/parse-number "123")))
    (is (= -456 (util/parse-number "-456")))
    (is (= 3.14 (util/parse-number "3.14")))
    (is (= -2.5 (util/parse-number "-2.5")))
    (is (= "abc" (util/parse-number "abc")))))

(deftest test-chinese-keyword?
  (testing "中文关键字检查"
    (is (util/chinese-keyword? "函数"))
    (is (util/chinese-keyword? "变量"))
    (is (util/chinese-keyword? "常量"))
    (is (not (util/chinese-keyword? "hello")))
    (is (not (util/chinese-keyword? "123")))))

(deftest test-extract-function-name
  (testing "提取函数名"
    (is (= "test" (util/extract-function-name "@test()")))
    (is (= "调试输出" (util/extract-function-name "@调试输出(a, b)")))
    (is (= "func" (util/extract-function-name "@func")))
    (is (nil? (util/extract-function-name "notfunction")))))

(deftest test-split-function-args
  (testing "分割函数参数"
    (is (= [] (util/split-function-args "")))
    (is (= ["a"] (util/split-function-args "a")))
    (is (= ["a" "b"] (util/split-function-args "a, b")))
    (is (= ["\"hello\"" "123"] (util/split-function-args "\"hello\", 123")))
    (is (= ["@func(a, b)" "c"] (util/split-function-args "@func(a, b), c")))))

(deftest test-format-error
  (testing "错误格式化"
    (let [error (util/format-error :test "测试错误")]
      (is (= :test (:error-type error)))
      (is (= "测试错误" (:message error)))
      (is (number? (:timestamp error))))
    
    (let [error (util/format-error :parse "解析错误: %s" "语法")]
      (is (= "解析错误: 语法" (:message error))))))

(deftest test-normalize-chinese-quotes
  (testing "标准化中文引号"
    (is (= "\"hello\"" (util/normalize-chinese-quotes "「hello」")))
    (is (= "\"world\"" (util/normalize-chinese-quotes "\\world\\")))
    (is (= "\"test\"" (util/normalize-chinese-quotes "\"test\"")))))

(run-tests)