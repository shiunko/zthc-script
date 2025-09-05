(ns net.zthc.script.parser.util
  (:require
   [clojure.string :as str]))

(defn remove-comments
  "移除代码中的注释"
  [code]
  (-> code
      ;; 移除单行注释 //
      (str/replace #"//.*" "")
      ;; 移除块注释 /* */
      (str/replace #"/\*[\s\S]*?\*/" "")))

(defn normalize-whitespace
  "标准化空白字符"
  [code]
  (-> code
      ;; 将多个空格合并为一个
      (str/replace #"\s+" " ")
      ;; 移除行首行尾空格
      str/trim))

(defn split-statements
  "分割语句"
  [code]
  (->> code
       remove-comments
       normalize-whitespace
       (#(str/split % #";"))
       (map str/trim)
       (filter #(not (empty? %)))))

(defn extract-function-parts
  "提取函数各部分"
  [func-def]
  (let [func-def (str/trim func-def)]
    (when (str/starts-with? func-def "函数")
      (let [content (str/trim (subs func-def 2))
            paren-start (str/index-of content "(")
            paren-end (str/index-of content ")")
            brace-start (str/index-of content "{")
            brace-end (str/last-index-of content "}")]

        (when (and paren-start paren-end brace-start brace-end
                   (< paren-start paren-end)
                   (< paren-end brace-start)
                   (< brace-start brace-end))
          {:name (str/trim (subs content 0 paren-start))
           :params (subs content (inc paren-start) paren-end)
           :body (subs content (inc brace-start) brace-end)})))))

(defn extract-variable-parts
  "提取变量定义各部分"
  [var-def]
  (let [var-def (str/trim var-def)]
    (when (or (str/starts-with? var-def "变量")
              (str/starts-with? var-def "常量"))
      (let [keyword-len (if (str/starts-with? var-def "变量") 2 2)
            content (str/trim (subs var-def keyword-len))
            colon-idx (str/index-of content ":")]

        (when colon-idx
          {:name (str/trim (subs content 0 colon-idx))
           :value (str/trim (subs content (inc colon-idx)))
           :type (if (str/starts-with? var-def "变量") :variable :constant)})))))

(defn parse-parameter-list
  "解析参数列表"
  [params-str]
  (if (empty? (str/trim params-str))
    []
    (->> (str/split params-str #",")
         (map str/trim)
         (filter #(not (empty? %)))
         vec)))

(defn balance-parentheses?
  "检查括号是否平衡"
  [s]
  (let [chars (seq s)]
    (loop [chars chars
           paren-count 0
           brace-count 0
           bracket-count 0
           in-string false
           string-char nil]
      (if (empty? chars)
        (and (= paren-count 0)
             (= brace-count 0)
             (= bracket-count 0)
             (not in-string))
        (let [c (first chars)
              remaining (rest chars)]
          (cond
            ;; 处理字符串
            (and (not in-string) (or (= c \") (= c \') (= c \「) (= c \\)))
            (recur remaining paren-count brace-count bracket-count true c)

            (and in-string (= c string-char))
            (recur remaining paren-count brace-count bracket-count false nil)

            in-string
            (recur remaining paren-count brace-count bracket-count in-string string-char)

            ;; 处理括号
            (= c \()
            (recur remaining (inc paren-count) brace-count bracket-count in-string string-char)

            (= c \))
            (recur remaining (dec paren-count) brace-count bracket-count in-string string-char)

            (= c \{)
            (recur remaining paren-count (inc brace-count) bracket-count in-string string-char)

            (= c \})
            (recur remaining paren-count (dec brace-count) bracket-count in-string string-char)

            (= c \[)
            (recur remaining paren-count brace-count (inc bracket-count) in-string string-char)

            (= c \])
            (recur remaining paren-count brace-count (dec bracket-count) in-string string-char)

            :else
            (recur remaining paren-count brace-count bracket-count in-string string-char)))))))

(defn validate-syntax
  "基本语法验证"
  [code]
  (let [errors []]
    (cond-> errors
      (not (balance-parentheses? code))
      (conj "括号不平衡")

      (empty? (str/trim code))
      (conj "代码不能为空"))))

(defn find-matching-bracket
  "找到匹配的括号位置"
  [s start-pos open-char close-char]
  (let [chars (seq s)]
    (loop [pos start-pos
           depth 0
           in-string false
           string-char nil]
      (if (>= pos (count chars))
        nil
        (let [c (nth chars pos)]
          (cond
            ;; 处理字符串
            (and (not in-string) (or (= c \") (= c \') (= c \「) (= c \\)))
            (recur (inc pos) depth true c)

            (and in-string (= c string-char))
            (recur (inc pos) depth false nil)

            in-string
            (recur (inc pos) depth in-string string-char)

            ;; 处理括号
            (= c open-char)
            (recur (inc pos) (inc depth) in-string string-char)

            (= c close-char)
            (if (= depth 1)
              pos
              (recur (inc pos) (dec depth) in-string string-char))

            :else
            (recur (inc pos) depth in-string string-char)))))))

(defn pretty-print-ast
  "美化打印AST"
  [ast & {:keys [indent] :or {indent 0}}]
  (let [spaces (str/join (repeat indent "  "))]
    (cond
      (map? ast)
      (do
        (println (str spaces "{"))
        (doseq [[k v] ast]
          (println (str spaces "  " k ": "))
          (pretty-print-ast v :indent (+ indent 2)))
        (println (str spaces "}")))

      (vector? ast)
      (do
        (println (str spaces "["))
        (doseq [item ast]
          (pretty-print-ast item :indent (+ indent 1)))
        (println (str spaces "]")))

      :else
      (println (str spaces (pr-str ast))))))
