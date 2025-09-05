(ns net.zthc.script.parser.util
  (:require [instaparse.core :as insta]))

(insta/defparser util-parser
                 (str
                   "<before> = <P> <Close>;"
                   "<after> = <Open> <P>;"
                   "<P> = #'[^']*';"
                   "<Close> = #'[^']*';"
                   "<Open> = #'[^']*';"))

(defn before
  [p close]
  (str p close))

(defn after
  [open p]
  (str open p))