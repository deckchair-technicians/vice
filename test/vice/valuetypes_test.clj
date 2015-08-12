(ns vice.valuetypes-test
  (:require [midje.sweet :refer :all]
            [vice
             [coerce :refer :all]
             [valuetypes :refer :all :as v]])
  (:import [java.net URL URI]))

(fact "UrlNoTrailingSlash"
  (validate UrlNoTrailingSlash "http://localhost/")
  => (URL. "http://localhost")

  (validate UrlNoTrailingSlash "http://localhost")
  => (URL. "http://localhost"))

(fact "Uri"
  (validate Uri"http://localhost")
  => (URI. "http://localhost"))

(facts "Iso currency"
  (validate Iso3LetterCurrency "AAA") => "AAA"
  (validate Iso3LetterCurrency "AAAB") => (throws Exception))

(fact "Double"
  (validate v/Doub "0.05")
  => 0.05

  (validate v/Doub 0.05M)
  => 0.05

  (validate v/Doub 0.05)
  => 0.05

  (validate v/Doub 1)
  => 1.0)

(fact "RePattern"
  (re-seq (validate RePattern "[ace]")
          "abcde")
  => ["a" "c" "e"])

(fact "CaseInsensitiveRePattern"
  (re-seq (validate CaseInsensitiveRePattern "[ace]")
          "AbcDe")
  => ["A" "c" "e"])
