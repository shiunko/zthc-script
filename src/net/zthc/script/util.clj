(ns net.zthc.script.util
  (:import (java.util Date))
  (:require [clojure.string :refer [upper-case lower-case]]))

(defmacro log
  "docstring"
  [level msg & params]
  `(println (format (str "[" (-> ~level name upper-case) "][" (Date.) "]: " ~msg) ~@params))
  )

(defn big-first 
  [^String word]  
  (let [len (count word)]    
    (str (-> word (.charAt 0) Character/toUpperCase Character/toString) 
      (String. (.toCharArray word) 1 (dec len)))))

(defn defn-from 
  [str mdata args & body]  
  `(defn ~(symbol str) ~mdata ~args ~@body))

(defmacro make-gets
  ([prefix state-map mdata]
   (cons `do
         (let [ks (keys state-map)]
           (for [i (range (count ks))]
             (defn-from (str prefix "get" (-> (nth ks i) name big-first)) mdata
                        '[this]
                        `(-> ~state-map (nth ~ks ~i)))))
         ))
  ([prefix state]
   (make-gets prefix state {})))

(defmacro def-string
  [& args]
  (when (-> args count odd?)
    (cons `do
          (for [i (range (count args))]
            (let [current (nth args i)
                  k (-> current keys first)
                  v (-> current vals first)]
              (println k v)
              `(def ~(with-meta k {:tag String}) ~v)
              )
            ))
    ))

(defmacro try-with-default
  [doing exception val]
  `(try ~doing
        (catch ~exception ~'e
          ~val))
  )