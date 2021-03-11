(ns zthc-script.parser.number
  (:refer-clojure
    :exclude [char float])
  (:require
    [the.parsatron :refer :all]))

(defparser float []
           (let->> [integral (many1 (digit))
                    _ (char \.)
                    fractional (many1 (digit))]
                   (let [integral (apply str integral)
                         fractional (apply str fractional)]
                     (always (Double/parseDouble (str integral "." fractional))))))

(defparser integer []
           (let->> [number (many1 (digit))]
                   (always (apply str number))))
