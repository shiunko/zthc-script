(ns net.zthc.script.parser.symbol
  (:require [instaparse.core :as insta]))

(insta/defparser symbol-parser
                 (str
                   "<blank> = #'[ \\t]*';"
                   "<blank1> = #'[ \\t]+';"
                   "<blank-line> = #'\\n*';"
                   "<end-function> = #';*';"
                   "<end-function1> = #';+';"
                   "<variable-split> = #'[ ]*','[ ]*';"))

(defn parse-blank [input]
  (insta/parse symbol-parser input))

(defn parse-blank1 [input]
  (insta/parse symbol-parser input))

(defn parse-blank-line [input]
  (insta/parse symbol-parser input))

(defn parse-end-function [input]
  (insta/parse symbol-parser input))

(defn parse-end-function1 [input]
  (insta/parse symbol-parser input))

(defn parse-variable-split [input]
  (insta/parse symbol-parser input))