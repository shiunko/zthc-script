(ns net.zthc.script.core
  (:require [net.zthc.script.tcp-server :as tcp]
            [net.zthc.script.util :as util]
            [clojure.tools.nrepl.server :as nrepl])
  (:gen-class))

(defn start-services
  "启动所有服务"
  []
  (println "启动 zthc-script 服务...")

  ;; 启动 nREPL 服务器
  (let [nrepl-port 7889]
    (println (str "启动 nREPL 服务器在端口 " nrepl-port))
    (nrepl/start-server :port nrepl-port))

  ;; 启动 TCP 服务器
  (let [tcp-port 7888]
    (println (str "启动 TCP 服务器在端口 " tcp-port))
    (tcp/start-server tcp-port))

  (println "所有服务已启动"))

(defn -main
  "主入口函数"
  [& args]
  (util/log :info "zthc-script 启动中...")

  (try
    (start-services)
    (util/log :info "zthc-script 已成功启动")

    ;; 保持程序运行
    (loop []
      (Thread/sleep 1000)
      (recur))

    (catch Exception e
      (util/log :error "启动失败: %s" (.getMessage e))
      (System/exit 1))))
