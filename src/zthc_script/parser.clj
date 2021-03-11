(ns zthc-script.parser
  (:refer-clojure
    :exclude [char float])
  (:require
    [the.parsatron :refer :all]
    [zthc-script.parser
     [symbol :refer :all]
     [string :refer :all]
     [number :refer :all]
     [util   :refer :all]
     ]))


(defparser function []
           (let->> [func (>> (blank) (after (char \@) (uword)))
                    _ (char \()
                    params (many (choice (uword) (integer) (float) (string-var) (variable-split) (function)))
                    _ (char \))
                    _ (>> (end-function)
                          (blank)
                          (blank-line))
                    ]
                   (always {:type :call :content {(apply str func) (filter #(not= \, %) params)}})
                   )
           )

(defparser def-var []
           (let->> [_ (blank)
                    prefix (choice (string "变量") (string "常量"))
                    _ (blank1)
                    var-name (uword)
                    _ (blank)
                    _ (choice (char \:) (char \：))
                    _ (blank)
                    value (before (choice (float) (integer) (string-var)) (>> (blank) (end-function1)))
                    _ (>> (blank)
                          (blank-line))]
                   (always {:type :var :content {(keyword (apply str var-name))  (apply str value)}})
                   ))

(defparser def-function []
           (let->> [_ (blank)
                    prefix (choice (string "函数") )
                    _ (blank1)
                    fun-name (uword)
                    _ (blank)
                    _ (>> (char \()
                          (blank))
                    params (many (choice (uword) (variable-split)))
                    _ (>> (char \))
                          (blank)
                          (char \{))
                    content (many (str-except2 \{ \}))
                    _ (>> (blank)
                          (char \})
                          (blank)
                          (blank-line))]
                   (always {:type :function :content {:name fun-name :params (filter #(not= \, %) params)
                                                      :content (apply str content)}}))
           )



(defn -main
  [& arglist]
  (let [line1 "@调试输出(asdxx, 123, @调试输出_(321, @到整数(\"777\")));"
        line2 "    变量 a: 199; 变量 b: \\asd123\\;"
        line3 "函数 add(a, b){ @返回(a+b) }"
        line4 "    变量 a: 1.99; 变量 b: \\hello\\;"
        text (concat line1 "\n" line2 "\n" line3 )]
    (println (run (before (choice (integer) (float)  (string-var)) (>> (blank) (end-function1))) "12.3;"))
    (println (run (function) line1))
    (println (run (many (def-var)) line2))
    (println (run (many (def-var)) line4))
    (println (run (def-function) line3))
    (println (run (many1 (choice (def-var) (def-function) (function))) text))
    )
  )