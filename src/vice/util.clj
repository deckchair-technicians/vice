(ns vice.util
  (:require [clj-time
             [core :as t]
             [format :as tf]])
  (:import [java.util UUID]
           [clojure.lang Keyword]
           [org.joda.time DateMidnight DateTime]))

(defmulti to-uuid class)
(defmethod to-uuid UUID [u] u)
(defmethod to-uuid String [s] (UUID/fromString s))

(defn uuid
  ([] (UUID/randomUUID))
  ([x]
   (to-uuid x)))

(defn camel-or-train-to-kebab [^String s]
  (-> s
      (clojure.string/replace #"([a-z])([A-Z])"
                              #(str (second %) \- (clojure.string/lower-case (second (next %)))))
      (clojure.string/replace "_" "-")
      (clojure.string/lower-case)))

(defn string->keyword [^String s]
  (-> s
      (clojure.string/replace #"[^a-zA-Z0-9]+"
                              "-")
      (clojure.string/replace #"-+$"
                              "")
      (camel-or-train-to-kebab)
      keyword))

(defmulti to-keyword class)
(defmethod to-keyword Keyword [x] x)
(defmethod to-keyword String [x] (string->keyword x))
(defmethod to-keyword nil [_] nil)

(def date-formatter (tf/formatter t/utc  "yyyy-MM-dd" "dd/MM/yyyy"))

(defn parse-date [s]
  (when s
    (DateMidnight/parse s date-formatter)))

(defn parse-date-time [s]
  (when s
    (if (instance? DateTime s)
      s
      (tf/parse (tf/formatters :date-time) s))))

(def date-time-formatter (tf/formatter t/utc
                                       "yyyy-MM-dd HH:mm"
                                       "yyyy-MM-dd HH:mm:ss"
                                       "yyyy-MM-dd'THH:mm"
                                       "yyyy-MM-dd'THH:mm:ss"))

(defn parse-any-date-time [s]
  (when s
    (if (instance? DateTime s)
      s
      (tf/parse date-time-formatter s))))

(defmulti date-midnight class)
(defmethod date-midnight DateMidnight [d] d)
(defmethod date-midnight DateTime [d] (DateMidnight. d))
(defmethod date-midnight String [s] (parse-date s))
(defmethod date-midnight nil [_] nil)

(defmulti date-time class)
(defmethod date-time DateTime [d] d)
(defmethod date-time String [s] (parse-date-time s))
(defmethod date-time nil [_] nil)

(defmulti integer class)
(defmethod integer int [i] i)
(defmethod integer Integer [i] i)
(defmethod integer Long [n] (int n))
(defmethod integer String [s] (Integer/parseInt s))
(defmethod integer nil [_] nil)

(defmulti bigdecimal
          "Only accepts nil, int, long, string and BigDecimal."
          class)
(defmethod bigdecimal BigDecimal [d] d)
(defmethod bigdecimal Long [s] (BigDecimal. s))
(defmethod bigdecimal Integer [s] (BigDecimal. s))
(defmethod bigdecimal String [s] (BigDecimal. s))
(defmethod bigdecimal nil [_] nil)

(defmulti number "Null-safe. Will coerce strings to BigDecimal" class)
(defmethod number Number [n] n)
(defmethod number String [s] (BigDecimal. s))
(defmethod number nil [_] nil)

(defn seqable?
  "Returns true if (seq x) will succeed, false otherwise."
  [x]
  (or (seq? x)
      (instance? clojure.lang.Seqable x)
      (nil? x)
      (instance? Iterable x)
      (-> x .getClass .isArray)
      (string? x)
      (instance? java.util.Map x)))
