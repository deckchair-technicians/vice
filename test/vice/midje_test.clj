(ns vice.midje-test
  (:require [midje.sweet :refer :all]
            [vice
             [midje :refer :all]]))

(fact "map schemas match loosely by default"
  {:a 123 :b 234} => (matches {:a Long}))
