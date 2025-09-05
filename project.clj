(defproject zthc-script "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :repositories [["central" "http://maven.aliyun.com/nexus/content/groups/public"]
                 ["clojars" "https://mirrors.tuna.tsinghua.edu.cn/clojars/"]
                 ["jcenter" "https://jcenter.bintray.com/"]]
  :dependencies [[org.clojure/clojure "1.12.2"]
                 ;[quil "3.1.0" :exclusions [org.clojure/clojure]]
                 [manifold "0.4.3"]
                 [org.fusesource.mqtt-client/mqtt-client "1.16"]
                 [org.clojure/data.json "2.5.1"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [instaparse "1.5.0"]]
  :profiles {:dev {:plugins [[lein-ancient "1.0.0-RC3"]
                             [org.clj-commons/lein-vizdeps "1.0"]]}}
  :main net.zthc.script.core)
(require 'cemerick.pomegranate.aether)
(cemerick.pomegranate.aether/register-wagon-factory!
  "http" #(org.apache.maven.wagon.providers.http.HttpWagon.))
