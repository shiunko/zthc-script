(ns net.zthc.script.tcp-client
  (:require [aleph.tcp :as tcp]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [net.zthc.script.util :as util]
            [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.net InetSocketAddress]))

(defn create-client
  "创建TCP客户端连接"
  ([host port] (create-client host port {}))
  ([host port options]
   (try
     (util/log :info "连接到服务器 %s:%d" host port)
     (let [stream @(tcp/client {:host host :port port})]
       (util/log :info "成功连接到服务器")
       {:stream stream
        :host host
        :port port
        :connected true
        :connect-time (System/currentTimeMillis)})
     (catch Exception e
       (util/log :error "连接服务器失败: %s" (.getMessage e))
       {:connected false
        :error (.getMessage e)}))))

(defn send-message
  "发送消息到服务器"
  [client message]
  (try
    (when (:connected client)
      (let [stream (:stream client)
            message-str (if (string? message)
                          message
                          (json/write-str message))]
        (s/put! stream (str message-str "\n"))
        (util/log :debug "消息已发送: %s" message-str)
        true))
    (catch Exception e
      (util/log :error "发送消息失败: %s" (.getMessage e))
      false)))

(defn receive-message
  "接收服务器消息"
  [client]
  (try
    (when (:connected client)
      (let [stream (:stream client)
            timeout-ms 5000
            result (deref (s/take! stream) timeout-ms :timeout)]
        (if (= result :timeout)
          {:timeout true}
          (let [message-str (if (string? result)
                              result
                              (String. result "UTF-8"))]
            {:message (str/trim message-str) :timestamp (System/currentTimeMillis)}))))
    (catch Exception e
      (util/log :error "接收消息失败: %s" (.getMessage e))
      {:error (.getMessage e)})))

(defn send-and-receive
  "发送消息并等待响应"
  [client message]
  (when (send-message client message)
    (receive-message client)))

(defn ping-server
  "ping服务器"
  [client]
  (send-and-receive client "ping"))

(defn parse-script
  "请求服务器解析脚本"
  [client script]
  (send-and-receive client (str "parse:" script)))

(defn execute-script
  "请求服务器执行脚本"
  [client script]
  (send-and-receive client (str "exec:" script)))

(defn execute-script-in-sandbox
  "请求服务器在沙箱中执行脚本"
  [client script]
  (send-and-receive client (str "sandbox:" script)))

(defn list-clients
  "列出服务器上的客户端"
  [client]
  (send-and-receive client "list-clients"))

(defn broadcast-message
  "广播消息"
  [client message]
  (send-and-receive client (str "broadcast:" message)))

(defn close-client
  "关闭客户端连接"
  [client]
  (try
    (when (:connected client)
      (s/close! (:stream client))
      (util/log :info "客户端连接已关闭"))
    (catch Exception e
      (util/log :error "关闭客户端失败: %s" (.getMessage e)))))

(defn get-client-info
  "获取客户端信息"
  [client]
  {:host (:host client)
   :port (:port client)
   :connected (:connected client)
   :connect-time (:connect-time client)
   :uptime (- (System/currentTimeMillis) (:connect-time client 0))})

(defn with-client
  "使用客户端执行操作，自动管理连接"
  [host port f]
  (let [client (create-client host port)]
    (if (:connected client)
      (try
        (f client)
        (finally
          (close-client client)))
      (util/log :error "无法连接到服务器"))))

;; 便捷函数
(defn quick-exec
  "快速执行脚本"
  [script & {:keys [host port] :or {host "127.0.0.1" port 7888}}]
  (with-client host port
    (fn [client]
      (let [response (execute-script client script)]
        (when response
          (:message response))))))

(defn quick-parse
  "快速解析脚本"
  [script & {:keys [host port] :or {host "127.0.0.1" port 7888}}]
  (with-client host port
    (fn [client]
      (let [response (parse-script client script)]
        (when response
          (:message response))))))