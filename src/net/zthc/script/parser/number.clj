(ns net.zthc.script.parser.number
  (:require [instaparse.core :as insta]))

(insta/defparser number-parser
                 (str
                   "number = float / integer;"
                   "float = #'[0-9]+[.][0-9]+';"
                   "integer = #'[0-9]+';"))

(defn parse-float [input]
  (let [result (insta/parse number-parser input)]
    (if (insta/failure? result)
      result
      (parse-double (get-in result [1 1] "0")))))

(defn parse-integer [input]
  (let [result (insta/parse number-parser input)]
    (if (insta/failure? result)
      result
      (parse-long (get-in result [1 1] "0")))))
