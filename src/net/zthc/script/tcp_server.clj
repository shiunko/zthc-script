(ns net.zthc.script.tcp-server
  (:require [aleph.tcp :as tcp]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [net.zthc.script.util :as util]
            [net.zthc.script.parser :as parser]
            [net.zthc.script.executor :as executor]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net InetSocketAddress]))

(def connected-clients (atom {}))

(defn client-id
  "生成客户端ID"
  [socket]
  (str (.getHostString (.getRemoteAddress socket))
       ":"
       (.getPort (.getRemoteAddress socket))))

(defn broadcast-message
  "广播消息给所有连接的客户端"
  [message]
  (doseq [[client-id stream] @connected-clients]
    (try
      (s/put! stream message)
      (util/log :debug "消息已发送给客户端 %s" client-id)
      (catch Exception e
        (util/log :error "发送消息给客户端 %s 失败: %s" client-id (.getMessage e))
        (swap! connected-clients dissoc client-id)))))

(defn send-response
  "发送响应给客户端"
  [stream response]
  (try
    (let [response-str (if (string? response)
                         response
                         (json/write-str response))]
      (s/put! stream (str response-str "\n")))
    (catch Exception e
      (util/log :error "发送响应失败: %s" (.getMessage e)))))

(defn process-script
  "处理脚本代码（仅解析）"
  [code]
  (try
    (let [result (parser/parse code)]
      {:success true
       :result result
       :message "脚本解析成功"})
    (catch Exception e
      (util/log :error "处理脚本失败: %s" (.getMessage e))
      {:success false
       :error (.getMessage e)
       :message "脚本解析失败"})))

(defn execute-script
  "执行脚本代码"
  [code]
  (try
    (let [result (executor/execute-script code parser/parse)]
      (if (:success result)
        {:success true
         :result (:last-result result)
         :all-results (:results result)
         :execution-count (:execution-count result)
         :message "脚本执行成功"}
        {:success false
         :error (:error result)
         :message "脚本执行失败"}))
    (catch Exception e
      (util/log :error "执行脚本失败: %s" (.getMessage e))
      {:success false
       :error (.getMessage e)
       :message "脚本执行失败"})))

(defn execute-script-in-sandbox
  "在沙箱中执行脚本"
  [code allowed-functions]
  (try
    (let [ast (parser/parse code)]
      (if (and (map? ast) (:error-type ast))
        {:success false
         :error "脚本解析失败"
         :parse-error ast}
        (let [code-forms (net.zthc.script.transformer/ast-to-clojure ast)
              result (executor/execute-in-sandbox code-forms allowed-functions)]
          (if (:success result)
            {:success true
             :result (:last-result result)
             :all-results (:results result)
             :sandbox true
             :message "沙箱执行成功"}
            {:success false
             :error (:error result)
             :sandbox true
             :message "沙箱执行失败"}))))
    (catch Exception e
      (util/log :error "沙箱执行失败: %s" (.getMessage e))
      {:success false
       :error (.getMessage e)
       :sandbox true
       :message "沙箱执行失败"})))

(defn process-command
  "处理客户端命令"
  [stream message client-id]
  (try
    (let [trimmed-msg (str/trim message)]
      (util/log :info "客户端 %s 发送消息: %s" client-id trimmed-msg)

      (cond
        ;; ping命令
        (= trimmed-msg "ping")
        (send-response stream {:type "pong" :timestamp (System/currentTimeMillis)})

        ;; 解析脚本命令
        (str/starts-with? trimmed-msg "parse:")
        (let [code (subs trimmed-msg 6)
              result (process-script code)]
          (send-response stream result))

        ;; 执行脚本命令
        (str/starts-with? trimmed-msg "exec:")
        (let [code (subs trimmed-msg 5)
              result (execute-script code)]
          (send-response stream result))

        ;; 沙箱执行命令
        (str/starts-with? trimmed-msg "sandbox:")
        (let [code (subs trimmed-msg 8)
              allowed-funcs ['调试输出 '打印 '输出 '到字符串 '到整数 '长度]
              result (execute-script-in-sandbox code allowed-funcs)]
          (send-response stream result))

        ;; 列出客户端命令
        (= trimmed-msg "list-clients")
        (send-response stream {:type "client-list"
                               :clients (keys @connected-clients)
                               :count (count @connected-clients)})

        ;; 广播命令
        (str/starts-with? trimmed-msg "broadcast:")
        (let [broadcast-msg (subs trimmed-msg 10)]
          (broadcast-message broadcast-msg)
          (send-response stream {:type "broadcast-sent" :message "消息已广播"}))

        ;; 默认回显
        :else
        (send-response stream {:type "echo"
                               :original message
                               :client-id client-id
                               :timestamp (System/currentTimeMillis)})))

    (catch Exception e
      (util/log :error "处理命令失败: %s" (.getMessage e))
      (send-response stream {:type "error"
                             :error (.getMessage e)
                             :message "命令处理失败"}))))

(defn handle-client
  "处理客户端连接"
  [stream info]
  (let [client-id (client-id (:socket info))]
    (util/log :info "新客户端连接: %s" client-id)

    ;; 添加到连接列表
    (swap! connected-clients assoc client-id stream)

    ;; 发送欢迎消息
    (send-response stream {:type "welcome"
                           :message "欢迎连接到 zthc-script 服务器"
                           :client-id client-id
                           :timestamp (System/currentTimeMillis)})

    ;; 处理消息流
    (d/loop [message (s/take! stream)]
      (d/chain message
               (fn [msg]
                 (when msg
                   (process-command stream msg client-id)
                   (d/recur (s/take! stream))))))

    ;; 连接关闭处理
    (s/on-closed stream
                 (fn []
                   (util/log :info "客户端断开连接: %s" client-id)
                   (swap! connected-clients dissoc client-id)))))

(defn start-server
  "启动TCP服务器"
  ([port] (start-server port "127.0.0.1"))
  ([port host]
   (try
     (util/log :info "启动TCP服务器 %s:%d" host port)

     (let [server (tcp/start-server handle-client
                                    {:port port
                                     :socket-address (InetSocketAddress. host port)})]

       (util/log :info "TCP服务器已启动在 %s:%d" host port)

       ;; 返回服务器对象以便后续管理
       {:server server
        :host host
        :port port
        :start-time (System/currentTimeMillis)})

     (catch Exception e
       (util/log :error "启动TCP服务器失败: %s" (.getMessage e))
       (throw e)))))

(defn stop-server
  "停止TCP服务器"
  [server-info]
  (try
    (when-let [server (:server server-info)]
      (.close server)
      (util/log :info "TCP服务器已停止"))

    ;; 清空客户端连接
    (reset! connected-clients {})

    (catch Exception e
      (util/log :error "停止TCP服务器失败: %s" (.getMessage e)))))

(defn get-server-stats
  "获取服务器统计信息"
  []
  {:connected-clients (count @connected-clients)
   :client-list (keys @connected-clients)
   :uptime (System/currentTimeMillis)})
