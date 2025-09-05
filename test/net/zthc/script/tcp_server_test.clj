(ns net.zthc.script.tcp-server-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.tcp-server :as server]
            [net.zthc.script.tcp-client :as client]))

(deftest test-server-lifecycle
  (testing "TCP服务器生命周期"
    ;; 启动服务器
    (let [test-server (server/start-server 7889)]
      (is (contains? test-server :server))
      (is (= 7889 (:port test-server)))
      (is (= "127.0.0.1" (:host test-server)))
      
      ;; 停止服务器
      (server/stop-server test-server)
      
      ;; 验证服务器统计
      (let [stats (server/get-server-stats)]
        (is (>= (:connected-clients stats) 0))))))

(deftest test-script-processing
  (testing "脚本处理功能"
    ;; 测试解析脚本
    (let [result (server/process-script "变量 x: 100;")]
      (is (:success result))
      (is (:result result)))
    
    ;; 测试执行脚本
    (let [result (server/execute-script "@调试输出(\"test\");")]
      (is (:success result))
      (is (= "test" (:result result))))
    
    ;; 测试函数定义和调用
    (let [result (server/execute-script "函数 double(x) { @返回(@乘(x, 2)); }; @调试输出(@double(5));")]
      (is (:success result))
      (is (= "10" (:result result))))
    
    ;; 测试沙箱执行
    (let [result (server/execute-script-in-sandbox "@调试输出(\"sandbox\");" ['调试输出])]
      (is (:success result))
      (is (:sandbox result)))))

(deftest test-client-server-integration
  (testing "客户端-服务器集成测试"
    ;; 启动测试服务器
    (let [test-server (server/start-server 7890)]
      (try
        ;; 等待服务器启动
        (Thread/sleep 500)
        
        ;; 创建客户端
        (let [test-client (client/create-client "127.0.0.1" 7890)]
          (when (:connected test-client)
            ;; 测试ping
            (let [ping-response (client/ping-server test-client)]
              (is (contains? ping-response :message)))
            
            ;; 测试脚本执行
            (let [exec-response (client/execute-script test-client "变量 y: 42; @调试输出(y);")]
              (is (contains? exec-response :message)))
            
            ;; 测试函数执行
            (let [func-response (client/execute-script test-client "函数 triple(n) { @返回(@乘(n, 3)); }; @调试输出(@triple(7));")]
              (is (contains? func-response :message)))
            
            ;; 关闭客户端
            (client/close-client test-client)))
        
        (finally
          ;; 停止服务器
          (server/stop-server test-server))))))

(deftest test-message-serialization
  (testing "消息序列化"
    ;; 测试序列化简单值
    (is (= "hello" (server/serialize-result "hello")))
    (is (= 42 (server/serialize-result 42)))
    (is (= true (server/serialize-result true)))
    (is (nil? (server/serialize-result nil)))
    
    ;; 测试复杂对象序列化
    (let [complex-result {:type "test" :value [1 2 3]}]
      (is (string? (server/serialize-result complex-result))))))

(run-tests)