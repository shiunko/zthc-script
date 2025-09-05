(ns net.zthc.script.core
  (:require
    ;[quil.core :as q]
    [net.zthc.script.util :as util]
    [clojure.tools.nrepl.server :refer [start-server stop-server]]
    [clojure.tools.nrepl.misc :refer [response-for]]
    [clojure.tools.nrepl.transport :as t])
  )

;(defn setup []
;  (q/smooth))
;
;(defn draw []
;  (q/background 255)
;  (q/fill 192)
;  (q/ellipse 100 100 30 30))
;
;(defn draw-ui
;  []
;  (q/defsketch example
;               :title "Example"
;               :setup setup
;               :draw draw
;               :size [200 200])
;  )

(defn current-time
  [h]
  (fn [{:keys [op transport] :as msg}]
    (if (= "time?" op)
      (t/send transport (response-for msg :status :done :time (System/currentTimeMillis)))
      (h msg))))

(defonce server (start-server
                  :port 7888
                  :handler current-time
                  ))

(defn -main [& args]
  (util/log :info "REPL Started! At[%s]" (-> server :server-socket))
  )
