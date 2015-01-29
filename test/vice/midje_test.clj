(ns vice.midje-test
  (:require [midje.sweet :refer :all]
            [vice
             [midje :refer :all]
             [schemas :refer :all]]))

(fact "map schemas match loosely by default"
  {:a 123 :b 234} => (matches {:a Long}))

(fact "schemas can be marked as strict"
  {:a 123 :not-in-schema 234}
  => (matches (is-not (strict {:a Long}))))


(fact "strictness cascades down"
  {:strict
   {:still-strict 123
    :oh-dear      456}}
  => (matches (is-not (strict {:strict
                               {:still-strict 123}}))))

(fact "strictness and looseness can be switched"
  {:strict {:loose 123
            :not-in-schema 123}}
  => (matches (strict {:strict
                       (loose {:loose Long})})))


