(ns net.zthc.script.parser
  (:require [instaparse.core :as insta]
            [net.zthc.script.parser.symbol :as symbol]
            [net.zthc.script.parser.string :as string]
            [net.zthc.script.parser.number :as number]
            [net.zthc.script.parser.util :as util]))

(insta/defparser main-parser
  (str
   "statement = function | def-var | def-function | if-statement | if-else-statement | for-loop;"
   "function = blank '@' uword '(' param-list ')' end-function blank-line;"
   "def-var = blank ('变量' | '常量') blank1 uword blank (':' | '：') blank value end-function blank-line;"
   "def-function = blank '函数' blank1 uword blank '(' param-list ')' blank '{' content '}' blank blank-line;"
   "if-statement = blank '如果' blank '(' condition ')' blank '{' content '}' blank blank-line;"
   "if-else-statement = blank '如果' blank '(' condition ')' blank '{' content '}' blank '否则' blank '{' content '}' blank blank-line;"
   "for-loop = blank '循环' blank '(' condition ')' blank '{' content '}' blank blank-line;"
   "param-list = (uword | integer | float | string-var | variable-split)*;"
   "value = float | integer | string-var;"
   "condition = #'[^)]*';"
   "content = #'[^{}]*';"
   "<blank> = #'[ \\t]*';"
   "<blank1> = #'[ \\t]+';"
   "<blank-line> = #'\\n*';"
   "<end-function> = #';*';"
   "<uword> = #'[a-zA-Z][a-zA-Z0-9_-]*';"
   "<integer> = #'[0-9]+';"
   "<float> = #'[0-9]+\\.[0-9]+';"
   "<string-var> = #'\\\"[^\\\"]*\\\"' | #\"'[^']*'\" | #'“[^“”]*”' | #'\\\\[^\\\\]*\\\\' | #' [^ ]* ';"
   "<variable-split> = #'[ ]*,[ ]*';"))

(defn parse-statement [input]
  (let [result (insta/parse main-parser input :start :statement)]
    (if (insta/failure? result)
      result
      (let [parsed-type (first result)]
        (case parsed-type
          :function {:type :call :content {(get-in result [1]) (filter #(not= \, %) (get-in result [3]))}}
          :def-var {:type :var :content {(keyword (get-in result [2])) (str (get-in result [5]))}}
          :def-function {:type :function :content {:name (get-in result [2])
                                                  :params (filter #(not= \, %) (get-in result [4]))
                                                  :content (apply str (get-in result [6]))}}
          :if-statement {:type :if :content {:condition (get-in result [2]) :body (get-in result [4])}}
          :if-else-statement {:type :if-else
                              :content {:condition (get-in result [2])
                                        :if-body (get-in result [4])
                                        :else-body (get-in result [6])}}
          :for-loop {:type :for-loop
                     :content {:condition (get-in result [2])
                               :body (get-in result [4])}}
          result)))))
