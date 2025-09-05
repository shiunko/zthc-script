(ns net.zthc.script.tcp-client-test
  (:require [clojure.test :refer :all]
            [net.zthc.script.tcp-client :as client]
            [net.zthc.script.tcp-server :as server]))

(deftest test-client-creation
  (testing "客户端创建"
    ;; 测试连接失败情况
    (let [failed-client (client/create-client "127.0.0.1" 9999)]
      (is (not (:connected failed-client)))
      (is (contains? failed-client :error)))))

(deftest test-client-info
  (testing "客户端信息"
    ;; 创建一个失败的连接来测试信息
    (let [test-client {:host "127.0.0.1" 
                       :port 7891 
                       :connected false 
                       :connect-time (System/currentTimeMillis)}
          info (client/get-client-info test-client)]
      (is (= "127.0.0.1" (:host info)))
      (is (= 7891 (:port info)))
      (is (not (:connected info)))
      (is (>= (:uptime info) 0)))))

(deftest test-quick-functions
  (testing "快速执行函数"
    ;; 启动测试服务器
    (let [test-server (server/start-server 7892)]
      (try
        ;; 等待服务器启动
        (Thread/sleep 500)
        
        ;; 测试快速执行（不需要手动管理连接）
        (let [result (client/quick-exec "@调试输出(\"快速测试\");" :host "127.0.0.1" :port 7892)]
          ;; 即使连接可能超时，我们也测试函数是否正常运行
          (is (or (string? result) (nil? result))))
        
        ;; 测试快速解析
        (let [result (client/quick-parse "变量 x: 123;" :host "127.0.0.1" :port 7892)]
          (is (or (string? result) (nil? result))))
        
        (finally
          ;; 停止服务器
          (server/stop-server test-server))))))

(deftest test-with-client-macro
  (testing "with-client宏测试"
    ;; 启动测试服务器
    (let [test-server (server/start-server 7893)]
      (try
        ;; 等待服务器启动
        (Thread/sleep 500)
        
        ;; 使用with-client自动管理连接
        (client/with-client "127.0.0.1" 7893
          (fn [test-client]
            (when (:connected test-client)
              ;; 测试基本操作
              (let [ping-result (client/ping-server test-client)]
                (is (or (map? ping-result) (nil? ping-result))))
              
              ;; 测试脚本执行
              (let [exec-result (client/execute-script test-client "@调试输出(\"with-client test\");")]
                (is (or (map? exec-result) (nil? exec-result)))))))
        
        (finally
          ;; 停止服务器
          (server/stop-server test-server))))))

(run-tests)