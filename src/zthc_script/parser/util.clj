(ns zthc-script.parser.util
  (:refer-clojure
    :exclude [char])
  (:require
    [the.parsatron :refer :all]))

(defn before
  [p close]
  (let->> [x p
           _ close]
          (always x)))

(defn after
  [open p]
  (let->> [_ open
           x p]
          (always x)))