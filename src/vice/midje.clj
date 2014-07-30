(ns vice.midje
  (:import [schema.utils ValidationError])
  (:require [schema
             [core :as s]
             [coerce :as c]
             [utils :as u]
             [macros :as m]]
            [clojure.walk :refer [postwalk]]
            [midje.checking.core :refer [as-data-laden-falsehood]]
            [clojure.pprint :refer [pprint]]
            [clojure.stacktrace :refer [print-cause-trace]]
            ))

(defn strict [schema]
  (with-meta schema {:toshtogo.test.midje-schema/strict true}))

(defn strict? [schema]
  (:toshtogo.test.midje-schema/strict (meta schema)))

(defn matcher [schema]
  (let [strictness-atom (atom nil)
        walk (s/walker
               (cond
                 (and (map? schema)
                      (not @strictness-atom))
                 (assoc schema s/Any s/Any)

                 (not (satisfies? s/Schema schema))
                 (s/eq schema)

                 :else
                 schema))]
    (fn [actual-value]
      (walk actual-value))))

(defn match [root-schema]
  (s/start-walker
    matcher
    root-schema))

(defn check [schema v]
  (-> v
      ((match schema))
      u/error-val))

(defn errors [schema x]
  "Produces a comparable map of schema validation errors, or nil"
  (postwalk (fn [e]
              (if (instance? ValidationError e)
                (u/validation-error-explain e)
                e))
            (check schema x)))

(defn matches [schema]
  (assert schema "No schema provided")
  (fn [v]
    (try
      (if-let [errors (check schema v)]
        (as-data-laden-falsehood {:notes [(with-out-str (pprint errors))]})
        true)

      (catch Throwable e
        (as-data-laden-falsehood {:notes [(with-out-str (print-cause-trace e))]})))))
