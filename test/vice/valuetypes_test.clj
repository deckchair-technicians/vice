(ns vice.valuetypes-test
  (:require [midje.sweet :refer :all]
            [vice
             [coerce :refer :all]
             [valuetypes :refer :all]])
  (:import [java.net URL URI]))

(fact "UrlNoTrailingSlash"
  (validate UrlNoTrailingSlash "http://localhost/")
  => (URL. "http://localhost")

  (validate UrlNoTrailingSlash "http://localhost")
  => (URL. "http://localhost"))

(fact "Uri"
  (validate Uri"http://localhost")
  => (URI. "http://localhost"))

(fact "RePattern"
  (re-seq (validate RePattern "[ace]")
          "abcde")
  => ["a" "c" "e"])

(fact "CaseInsensitiveRePattern"
  (re-seq (validate CaseInsensitiveRePattern "[ace]")
          "AbcDe")
  => ["A" "c" "e"])

