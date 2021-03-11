(ns zthc-script.parser.string
  (:refer-clojure
    :exclude [char])
  (:require
    [the.parsatron :refer :all]))

(defparser str-except [arg]
           (token #(and (char? %) (not= % arg))))

(defparser str-except2 [arg1 arg2]
           (token #(and (char? %) (not= % arg1) (not= % arg2))))

(defparser string-var_ [prefix endfix]
           (let->> [_ (char prefix)
                    content (many (str-except2 prefix endfix))
                    _ (char endfix)]
                   (always (str "\"" (apply str content) "\""))))

(defparser string-var []
           (choice (string-var_ \" \")
                   (string-var_ \' \')
                   (string-var_ \“ \”)
                   (string-var_ \\ \\)
                   ))


(defparser word []
           (many1 (letter)))

(defparser uword []
           (let->> [prefix (letter)
                    res (many (choice
                                (letter)
                                (char \-)
                                (char \_)))]
                   (always (apply str prefix res))))