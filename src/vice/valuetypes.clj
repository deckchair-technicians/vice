(ns vice.valuetypes
  (:require [schema.core :as s]
            [vice
             [coerce :refer :all]
             [util :refer :all]])
  (:import [org.joda.time DateMidnight DateTime]
           [schema.core EnumSchema]
           [java.net URL]
           [java.util.regex Pattern]))

(def is-empty (s/pred empty? "should be empty"))

(defn keyword-enum [& vs]
  (with-coercion
    to-keyword
    (EnumSchema. (set vs))))

(def JodaDateMidnight (with-coercion
                        date-midnight
                        DateMidnight))

(def JodaDateTime (with-coercion
                    date-time
                    DateTime))

(def Positive (s/pred #(>= % 0M) "positive number"))

(def Int (with-coercion
           integer
           Integer))

(def PositiveInteger (s/both Int Positive))

(def Num (with-coercion
           number
           s/Num))

(def PositiveNum (s/both Num Positive))

(def BigDec
  (with-coercion
    bigdecimal
    BigDecimal))

(def PositiveBigDec (s/both BigDec Positive))


(def Uuid (with-coercion
            uuid
            s/Uuid))

(def GenUuid (with-coercion
               #(if % % (uuid))
               s/Uuid))

(def Str (with-coercion
           #(if (nil? %) nil (str %))
           s/Str))

(def Url (s/both Str
                 (with-coercion
                   #(URL. %)
                   URL)))


(def RePattern (with-coercion
                 re-pattern
                 Pattern))

(def CaseInsensitiveRePattern (with-coercion
                                #(str "(?i)" %)
                                RePattern))

(def Iso2LetterCountry #"[A-Z]{2}")
(def Iso3LetterCurrency #"[A-Z]{3}")
(def UrlNoTrailingSlash (s/both
                          Url
                          (with-coercion
                            #(strip-trailing-slash (str %))
                            Url)))

(defn- arity
  [f]
  (-> f class .getDeclaredMethods first .getParameterTypes alength))

(def Function (s/pred fn? "function?"))

; These are not well defined for functions with multiple arities

(def ArityZeroFunction (s/both Function (s/pred #(= 0 (arity %)) "arity zero?")))
(def NullaryFunction ArityZeroFunction)

(def ArityOneFunction (s/both Function (s/pred #(= 1 (arity %)) "arity one?")))
(def UnaryFunction ArityOneFunction)

(def ArityTwoFunction (s/both Function (s/pred #(= 2 (arity %)) "arity two?")))
(def BinaryFunction ArityTwoFunction)
