(ns net.zthc.script.early-return
  "早期返回异常类型，用于处理函数中的早期返回语句")

(def ^:const return-type "EarlyReturn")

(defn ->EarlyReturn
  "创建EarlyReturn异常"
  [value]
  (ex-info "do return" {:value value
                        :type  return-type}))

(defn early-return?
  "检查是否为EarlyReturn异常"
  [e]
  (= return-type (:type (ex-data e))))