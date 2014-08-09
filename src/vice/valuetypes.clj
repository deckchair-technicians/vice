(ns vice.valuetypes
  (:require [schema.core :as s]
            [vice
             [coerce :refer :all]
             [util :refer :all]])
  (:import [org.joda.time DateMidnight DateTime]
           [schema.core EnumSchema]
           [java.net URL]))

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
           str
           s/Str))

(def Url (s/both Str
                 (with-coercion
                   #(URL. %)
                   Str)))

(def Iso2LetterCountry #"[A-Z]{2}")
(def Iso3LetterCurrency #"[A-Z]{3}")
(def UrlNoTrailingSlash (s/both
                          Url
                          (with-coercion
                            strip-trailing-slash
                            Url)))
