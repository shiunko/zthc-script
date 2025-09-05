(ns net.zthc.script.tcp-server
  (:require [manifold.stream :as s]
            [manifold.deferred :as d]
            [aleph.tcp :as tcp])
  (:refer [net.zthc.script.util :refers [log]]))

(defrecord Server [server connections])

(defn last-str
  [^String text x]
  (if (> (count text) x)
    (subs text (- (count text) x))
    text))

(defn handle-client [socket client-address]
  (try
    (log :info (str "[JOIN][" client-address "]"))
    (let [in (s/source->stream socket)
          out (s/sink->stream socket)]
      (d/loop []
        (d/chain (s/take! in ::drained)
          (fn [msg]
            (when-not (= ::drained msg)
              (log :info (str "[RECV][" client-address "] " msg))
              (s/put! out msg)
              (d/recur)))))
      (d/catch Exception e
        (log :error (str "Client error: " e))))
    (finally
      (log :info (str "[EXIT][" client-address "]"))
      (s/close! socket))))

(defn close-server [{:keys [server connections]}]
  (doseq [conn @connections]
    (try
      (s/close! conn)
      (catch Exception e)))
  (try
    (.close server)
    (catch Exception e)))

(defn start-server
  ([port host]
   (let [server (tcp/start-server
                  (fn [socket info]
                    (let [client-address (:remote-address info)]
                      (handle-client socket client-address)))
                  {:port port
                   :host host
                   :socket-options {:reuse-addr true}})
         connections (atom [])]
     (log :info (str "Server started on " host ":" port))
     (->Server server connections)))
  ([port]
   (start-server port "127.0.0.1")))
