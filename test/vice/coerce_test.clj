(ns vice.coerce-test
  (:require [midje.sweet :refer :all]
            [schema
             [core :as s]
             [utils :refer [validation-error-explain]]]
            [vice
             [midje :refer :all]
             [coerce :as vc]
             [schemas :as vs]
             [util :refer :all]])
  (:import [clojure.lang ExceptionInfo Symbol]
           [schema.utils ValidationError]))

(def test-schema
  (s/both {:a s/Num
           s/Any s/Any}
          (s/conditional #(= (:type %) :foo) {:foo-param s/Str
                                              s/Any s/Any})))

(fact (s/check test-schema {:a 1, :type :foo, :foo-param "params"}) => nil)

(facts "validated and check both work with with-coercion"
  (fact "Uses coercer"
    (vc/validate {:a (vc/with-coercion str s/Str)} {:a 123})
    => {:a "123"})

  (fact "Reports errors in coercer"
    (let [e (RuntimeException. "some validation problem")]
      (vc/errors (vc/with-coercion (fn [_] (throw e)) s/Str)
              "aaa")
      => (list 'not e))))

(facts "validators work as expected"
  (let [schema {:a s/Int}
        value  {:a 1.2}]
    (try
      (vc/validate schema value)
      (vs/fail "Expected exception")
      (catch ExceptionInfo e
        (fact "Message"
          (.getMessage e)
          => "Value does not match schema: {:a (not (integer? 1.2))}")

        (fact "ex-data contains schema, value and error"
          (ex-data e)
          => (matches {:schema {:a s/Any}
                       :value  value
                       :error  {:a ValidationError}}))))))
