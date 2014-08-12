(ns vice.valuetypes-test
  (:require [midje.sweet :refer :all]
            [vice
             [coerce :refer :all]
             [valuetypes :refer :all]]))

(fact "UrlNoTrailingSlash"
  (validate UrlNoTrailingSlash "http://localhost/")
  => "http://localhost"

  (validate UrlNoTrailingSlash "http://localhost")
  => "http://localhost")

(fact "RePattern"
  (re-seq (validate RePattern "[ace]")
          "abcde")
  => ["a" "c" "e"])

(fact "CaseInsensitiveRePattern"
  (re-seq (validate CaseInsensitiveRePattern "[ace]")
          "AbcDe")
  => ["A" "c" "e"])

