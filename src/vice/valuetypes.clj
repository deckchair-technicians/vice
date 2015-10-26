(ns vice.valuetypes
  (:require [schema.core :as s]
            [vice
             [coerce :refer :all]
             [util :refer :all]])
  (:import [org.joda.time DateMidnight DateTime]
           [schema.core EnumSchema]
           [java.net URL URI]
           [java.util.regex Pattern]))

(def is-empty (s/pred empty? "should be empty"))

(defn keyword-enum [& vs]
  (with-coercion
    to-keyword
    (EnumSchema. (set vs))))

(def JodaDateMidnight (with-coercion
                        coerce-date-midnight
                        DateMidnight))

(def JodaDateTime (with-coercion
                    coerce-date-time
                    DateTime))

(def Positive (s/pred #(>= % 0M) "positive number"))

(def Int (with-coercion
           coerce-integer
           Integer))

(def PositiveInteger (s/both Int Positive))

(def Num (with-coercion
           coerce-number
           s/Num))

(def Doub (with-coercion
              coerce-double
              Double))

(defmulti ->boolean class)
(defmethod ->boolean Boolean [x] x)
(defmethod ->boolean String [x]
  (case x
    "true" true
    "false" false))

(def Bool
  (with-coercion
    ->boolean
    Boolean))

(def PositiveNum (s/both Num Positive))

(def BigDec
  (with-coercion
    coerce-bigdecimal
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

(defmulti url class)
(defmethod url URL [x] x)
(defmethod url URI [x] (.toURL x))
(defmethod url String [x] (URL. x))

(def Url (with-coercion
           #(url %)
           URL))

(defmulti uri class)
(defmethod uri URI [x] x)
(defmethod uri URL [x] (.toURI x))
(defmethod uri String [x] (URI. x))

(def Uri (with-coercion
           #(uri %)
           URI))

(def RePattern (with-coercion
                 re-pattern
                 Pattern))

(def CaseInsensitiveRePattern (with-coercion
                                #(str "(?i)" %)
                                RePattern))

(def Iso2LetterCountry #"^[A-Z]{2}$")
(def Iso3LetterCurrency #"^[A-Z]{3}$")
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
