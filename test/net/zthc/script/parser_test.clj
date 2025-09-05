(ns net.zthc.script.parser-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.parser :as parser]))

(deftest parser-statement-test
  (testing "Testing Unified Statement Parser"
    (are [input expected] (= expected (parser/parse-statement input))

      "如果(a > 10) { @调试输出(\"a大于10\") }"
      {:type :if :content {:condition "a > 10" :body "@调试输出(\"a大于10\")"}}

      "如果(a > 10) { @调试输出(\"a大于10\") } 否则 { @调试输出(\"a小于等于10\") }"
      {:type :if-else
       :content {:condition "a > 10"
                 :if-body "@调试输出(\"a大于10\")"
                 :else-body "@调试输出(\"a小于等于10\")"}}

      "循环(a < 10) { @调试输出(\"循环中\") }"
      {:type :for-loop
       :content {:condition "a < 10"
                 :body "@调试输出(\"循环中\")"}}

      "@调试输出(asdxx, 123);"
      {:type :call :content {"调试输出" ["asdxx" "123"]}}

      "变量 a: 199;"
      {:type :var :content {:a "199"}}

      "函数 add(a, b){ @返回(a+b) }"
      {:type :function :content {:name "add" :params ["a" "b"] :content "@返回(a+b)"}})))