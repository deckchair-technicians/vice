(ns vice.coerce
  (:require [clojure.walk :refer [postwalk]]

            [schema
             [coerce :as c]
             [core :as s]
             [macros :as m]
             [utils :as u]])
  (:import [org.joda.time DateMidnight DateTime]
           [schema.core Schema]
           [schema.utils ValidationError]))

(defprotocol HasCoercer
  (get-coercer [this]))

(defrecord CoercingSchema [schema coercer]
  s/Schema
  (walker [this]
    (s/subschema-walker schema))
  (explain [this]
    (s/explain schema))

  HasCoercer
  (get-coercer [this]
    coercer))

(defn with-coercion [coercer schema]
  (CoercingSchema. schema coercer))

(defn coercion-matcher [schema]
  (if (satisfies? HasCoercer schema)
    (get-coercer schema)
    (c/json-coercion-matcher schema)))

(defn coercer
  "Returns a function that takes a value and returns either the coerced value,
   or an ErrorContainer"
  [schema]
  (c/coercer schema coercion-matcher))

(defn check
  "Returns nil or error-val"
  [schema value]
  (-> value
      ((coercer schema))
      (u/error-val)))

(defn errors [schema x]
  "Produces a comparable map of schema validation errors, or nil"
  (postwalk (fn [e]
              (if (instance? ValidationError e)
                (u/validation-error-explain e)
                e))
            (check schema x)))

(defn validator [schema]
  (let [c (coercer schema)]
    (fn [value]
      (let [r (c value)]
        (if-let [ev (u/error-val r)]
          (m/error! (u/format* "Value does not match schema: %s" (pr-str ev))
                    {:schema schema :value value :error ev})
          r)))))

(defn validate
  [schema value]
  ((validator schema) value ))
