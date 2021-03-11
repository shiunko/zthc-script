(ns zthc-script.parser.symbol
  (:refer-clojure
    :exclude [char])
  (:require
    [the.parsatron :refer :all]))


(defparser blank []
           (many (token #{\  \tab})))

(defparser blank1 []
           (many1 (token #{\  \tab})))

(defparser blank-line []
           (many (char \newline)))

(defparser end-function []
           (many (char \;)))

(defparser end-function1 []
           (many1 (char \;)))

(defparser variable-split []
           (let->> [_ (many (char \ ))
                    content (char \,)
                    _ (many (char \ ))]
                   (always content)))