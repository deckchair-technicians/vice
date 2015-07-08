(ns vice.schemas-test
  (:require [midje.sweet :refer :all]
            [schema.core :as s]
            [vice
             [midje :as vm]
             [coerce :refer :all]
             [schemas :refer :all]]))

(fact "always-a-seq"
  ((coercer (always-a-seq String)) "a") => ["a"]
  ((coercer (always-a-seq String)) ["a"]) => ["a"])

(fact "in-any-order: too many elements"
  (vm/errors {:a (in-any-order [2 1])}
          {:a [3 1 2 4]})
  => {:a ['(not (present? 3))
          '(not (has-extra-elts? (4)))]})

(fact "in-any-order: too many matchers"
  (vm/errors {:a (in-any-order [2 3 1])}
          {:a [1 2]})
  => {:a ['(not (missing-items? 3))]})

(fact "in-any-order: match"
  (vm/errors {:a (in-any-order [2 1])}
          {:a [1 2]})
  => nil)

(fact "in-any-order: no match"
  (vm/errors {:a (in-any-order [3 1])}
          {:a [1 2]})
  => {:a ['(not (present? 2))
          '(not (missing-items? 3))]})

(fact "in-any-order: nested maps"
  (vm/errors
    {:a (in-any-order [{:b 1} {:b 2}])}
    {:a [{:b 1} {:b 2}]})
  => nil)

(fact "in-order: too many elements"
  (vm/errors {:a (in-order [1 2])}
          {:a [1 2 3]})
  => {:a ['(not (has-extra-elts? (3)))]})

(fact "in-order: too many matchers"
  (vm/errors {:a (in-order [1 2 3 4])}
          {:a [1 2]})
  => {:a ['(not (missing-items? 3 4))]})

(fact "in-order: match"
  (vm/errors {:a (in-order [1 2])}
          {:a [1 2]})
  => nil)

(fact "in-order: wrong order"
  (vm/errors {:a (in-order [1 2])}
          {:a [2 1]})
  => {:a ['(not (= 1 2))
          '(not (= 2 1))]})

(fact "in-order: nested maps"
  (vm/errors
    {:a (in-order [{:b 1} {:b 2}])}
    {:a [{:b 1} {:b 2}]})
  => nil)

(fact "is-not"
  (vm/errors
    (is-not (s/eq 1))
    2)
  => nil

  (vm/errors
    (is-not (s/eq 1))
    1)
  => '(not (is-not (eq 1) 1)))

(fact "fail"
  (vm/errors
    (fail 'message)
    "some value")
  => '(not (message "some value")))


(facts "conditional-on-key"
  (let [s (conditional-on-key
            :vehicle-type s/Keyword

            :car
            {:wheels (s/eq 4)}

            :bike
            {:wheels (s/eq 2)})]

    (check s
           {:vehicle-type :car
            :wheels       4})
    => nil

    (check s
           {:vehicle-type :car
            :wheels       4})
    => nil

    (vm/errors s {:vehicle-type :car
               :wheels       2})
    => {:wheels '(not (= 4 2))}

    (vm/errors s {:vehicle-type :velocipede
               :wheels       4})
    => '(not ((no-matching :vehicle-type) {:vehicle-type :velocipede, :wheels 4}))))
