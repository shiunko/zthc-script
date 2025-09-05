(require '[net.zthc.script.parser :as parser])
(require '[net.zthc.script.executor :as executor])
(require '[net.zthc.script.util :as util])
(require '[net.zthc.script.parser.util :as parser-util])

(println "=== zthc-script 执行演示 ===")

;; 测试 1: 简单变量定义和使用
(println "\n1. 变量定义和调试输出:")
(let [code "变量 name: \"张三\"; @调试输出(\"你好\", name);"
      result (executor/execute-script code parser/parse)]
  (if (:success result)
    (println "✅ 执行成功，结果:" (:last-result result))
    (println "❌ 执行失败:" (:error result))))

;; 测试 2: 数学运算
(println "\n2. 数学运算:")
(let [code "变量 a: 10; 变量 b: 20; @调试输出(\"结果:\", @加(a, b));"
      result (executor/execute-script code parser/parse)]
  (if (:success result)
    (println "✅ 执行成功，结果:" (:last-result result))
    (println "❌ 执行失败:" (:error result))))

;; 测试 3: 类型转换
(println "\n3. 类型转换:")
(let [code "变量 num_str: \"123\"; 变量 num: @到整数(num_str); @调试输出(\"转换后:\", num);"
      result (executor/execute-script code parser/parse)]
  (if (:success result)
    (println "✅ 执行成功，结果:" (:last-result result))
    (println "❌ 执行失败:" (:error result))))

;; 测试 4: 比较操作
(println "\n4. 比较操作:")
(let [code "变量 x: 15; 变量 y: 10; @调试输出(\"x > y:\", @大于?(x, y));"
      result (executor/execute-script code parser/parse)]
  (if (:success result)
    (println "✅ 执行成功，结果:" (:last-result result))
    (println "❌ 执行失败:" (:error result))))

;; 测试 5: 字符串操作
(println "\n5. 字符串操作:")
(let [code "变量 text: \"Hello World\"; @调试输出(\"长度:\", @长度(text));"
      result (executor/execute-script code parser/parse)]
  (if (:success result)
    (println "✅ 执行成功，结果:" (:last-result result))
    (println "❌ 执行失败:" (:error result))))

;; 测试 6: 沙箱环境
(println "\n6. 沙箱环境测试:")
(let [ast (parser/parse "@调试输出(\"沙箱中运行\");")
      code-forms (net.zthc.script.transformer/ast-to-clojure ast)
      allowed-funcs ['调试输出 '打印]
      result (executor/execute-in-sandbox code-forms allowed-funcs)]
  (if (:success result)
    (println "✅ 沙箱执行成功，结果:" (:last-result result))
    (println "❌ 沙箱执行失败:" (:error result))))

(println "\n演示完成！🎉")