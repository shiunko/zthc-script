(defproject zthc-script "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repositories [["central" "http://maven.aliyun.com/nexus/content/groups/public"]
                 ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]
                 ["jcenter" "https://jcenter.bintray.com/"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 ;[quil "3.1.0" :exclusions [org.clojure/clojure]]
                 [io.netty/netty-all "4.1.56.Final"]
                 [org.fusesource.mqtt-client/mqtt-client "1.16"]
                 [org.clojure/data.json "1.0.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [the/parsatron "0.0.8"]]
  :main zthc-script.core)
(require 'cemerick.pomegranate.aether)
(cemerick.pomegranate.aether/register-wagon-factory!
  "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))
