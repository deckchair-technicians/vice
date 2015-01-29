(ns vice.midje
  (:require [schema
             [core :as s]
             [coerce :as c]
             [utils :as u]
             [macros :as m]]
            [clojure.walk :refer [postwalk]]
            [midje.checking.core :refer [as-data-laden-falsehood]]
            [clojure.pprint :refer [pprint]]
            [clojure.stacktrace :refer [print-cause-trace]])
  (:import [schema.utils ValidationError]))

(defn strict [schema]
  (with-meta schema {::match-mode :strict}))

(defn loose [schema]
  (with-meta schema {::match-mode :loose}))

(defn match-mode [schema]
  (::match-mode (meta schema)))

(defn matcher [strictness-atom schema]
  (when (not (nil? (match-mode schema)))
    (reset! strictness-atom (match-mode schema)))

  (let [walk (s/walker
               (cond
                 (and (map? schema)
                      (not= :strict @strictness-atom))
                 (assoc schema s/Any s/Any)

                 (not (satisfies? s/Schema schema))
                 (s/eq schema)

                 :else
                 schema))]
    (fn [actual-value]
      (walk actual-value))))

(defn match [root-schema]
  (s/start-walker
    (partial matcher (atom :loose))
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

(def matches-strict (comp matches strict))
