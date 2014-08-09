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
