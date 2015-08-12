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

(defn strip-trailing-slash [s]
  (clojure.string/replace s #"/$" ""))

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

(defmulti coerce-date-midnight class)
(defmethod coerce-date-midnight DateMidnight [d] d)
(defmethod coerce-date-midnight DateTime [d] (DateMidnight. d))
(defmethod coerce-date-midnight String [s] (parse-date s))
(defmethod coerce-date-midnight nil [_] nil)

(defmulti coerce-date-time class)
(defmethod coerce-date-time DateTime [d] d)
(defmethod coerce-date-time String [s] (parse-date-time s))
(defmethod coerce-date-time nil [_] nil)

(defmulti coerce-integer class)
(defmethod coerce-integer int [i] i)
(defmethod coerce-integer Integer [i] i)
(defmethod coerce-integer Long [n] (int n))
(defmethod coerce-integer String [s] (Integer/parseInt s))
(defmethod coerce-integer nil [_] nil)

(defmulti coerce-double "Null-safe. Will parse strings to Double. Will coerce other number types to double" class)
(defmethod coerce-double Double [n] n)
(defmethod coerce-double Number [n] (double n))
(defmethod coerce-double String [s] (Double/parseDouble s))
(defmethod coerce-double nil [_] nil)

(defmulti coerce-bigdecimal
          "Only accepts nil, int, long, string and BigDecimal."
          class)
(defmethod coerce-bigdecimal BigDecimal [d] d)
(defmethod coerce-bigdecimal Long [s] (BigDecimal. s))
(defmethod coerce-bigdecimal Integer [s] (BigDecimal. s))
(defmethod coerce-bigdecimal String [s] (BigDecimal. s))
(defmethod coerce-bigdecimal nil [_] nil)

(defmulti coerce-number "Null-safe. Will coerce strings to BigDecimal" class)
(defmethod coerce-number Number [n] n)
(defmethod coerce-number String [s] (BigDecimal. s))
(defmethod coerce-number nil [_] nil)

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

