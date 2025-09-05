(ns net.zthc.script.parser.string
  (:require [clojure.string :as str]
            [instaparse.core :as insta]))

(insta/defparser string-parser
                 (str
                   "string-var = string-var-space | string-var-single | string-var-double | string-var-chinese | string-var-backslash;"
                   "string-var-space = <' '> #'[^ ]*' <' '>;"
                   "string-var-single = <\"'\"> #\"[^']*\" <\"'\">;"
                   "string-var-double = <'\"'> #'[^\"]*' <'\"'>;"
                   "string-var-chinese = <'“'> #'[^“^”]*' <'”'>;"
                   "string-var-backslash = <'\\\\'> #'[^\\\\]*' <'\\\\'>;"))

(defn parse-string-var [input]
  (insta/parse string-parser input))