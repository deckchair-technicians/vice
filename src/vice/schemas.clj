(ns vice.schemas
  (:require [schema
             [core :as s]
             [macros :as m]
             [utils :as u]]
            [vice
             [coerce :refer :all]
             [schemas :refer :all]
             [util :refer :all]]))

(deftype FailSchema [expectation]
  s/Schema
  (walker [this]
    (fn [x]
      (m/validation-error this x (list expectation x))))
  (explain [this]
    expectation))

(defn fail [expectation]
  (FailSchema. expectation))

(defrecord NotSchema [s]
  s/Schema
  (walker [this]
    (let [walker (s/subschema-walker s)]
      (fn [x]
        (if (u/error? (walker x))
          x
          (m/validation-error this x (list 'is-not (s/explain s) (u/value-name x)))))))
  (explain [this] (list 'not (s/explain s))))

(defn is-not [expectation]
  (NotSchema. expectation))

(defrecord BetweenSchema [from to]
  s/Schema
  (walker [this]
    (fn [x]
      (if (>= to x from)
        x
        (m/validation-error this x (list 'between from to (u/value-name x))))))
  (explain [this] (list 'between from to)))

(defn between [from to]
  (BetweenSchema. from to))

(defn conditional-on-key
  "Returns a schema that selects from a list of map schemas based on the key value.

  You specify a schema for the key which is applied before the conditions are checked. If the
  key fails validation, then no other schemas will be run.

  Implicitly adds an :else to the end that returns a helpful error message.

  There is no need to re-specify the key value in the other schemas if they are maps.

  Usage:

  (conditional-on-key
      :vehicle-type s/Keyword

      :car
      {:wheels (s/eq 4)}

      :bike
      {:wheels (s/eq 2)})

  "
  [k key-schema & values-and-schemas]
  (assert (even? (count values-and-schemas)) "Must be an even number of matching values and schemas")
  (s/both {k key-schema
           s/Any s/Any}
          (apply s/conditional
                 (->> values-and-schemas
                       (partition 2)
                       (map (fn [[matching-value schema]]
                              (let [cond-fn #(= matching-value (k %))
                                    schema+key-value (if (map? schema)
                                                       (assoc schema k (s/eq matching-value))
                                                       schema)]
                                [cond-fn schema+key-value])))
                       (#(concat % [[:else (fail (list 'no-matching k))]]))
                       (mapcat identity)
                       ))))

(defn build-schemas [item-schemas]
  (vec (map-indexed (fn [i s] {:schema s
                               :name   (str "item " i)
                               :walker (s/subschema-walker s)})
                    item-schemas)))

(defn in-any-order [schemas & {:keys [extras-ok] :as opts}]
  (reify s/Schema
    (walker [this]
      (let [item-schemas (build-schemas schemas)
            err-conj (u/result-builder (constantly []))]
        (fn [xs]
          (if-not (seqable? xs)
            (m/validation-error this xs (list 'seq? xs))
            (loop [xs xs
                   remaining-item-schemas item-schemas
                   out []]
              (if (empty? remaining-item-schemas)

                (if (empty? xs)
                  ; no remaining schemas, no remaining items
                  out

                  ; more items than schemas
                  (if extras-ok
                    out
                    (err-conj out (m/validation-error nil xs (list 'has-extra-elts? xs)))))

                (if (empty? xs)
                  ; more schemas than items
                  (err-conj out
                            (m/validation-error
                              (vec (map :schema remaining-item-schemas))
                              nil
                              (list* 'missing-items? (map :schema remaining-item-schemas))))

                  (let [x (first xs)
                        match (->> remaining-item-schemas
                                   (filter (fn [item-schema]
                                             (not (u/error-val ((:walker item-schema) x)))))
                                   first)]
                    (recur (rest xs)
                           (remove #{match} remaining-item-schemas)
                           (if (or extras-ok match)
                             out
                             (err-conj out
                                       (m/validation-error
                                         nil
                                         xs
                                         (list* 'present?
                                                [x])))))))))))))

    (explain [this]
      (list 'in-any-order
            opts
            (doall
              (map-indexed (fn [schema index]
                             (if (satisfies? s/Schema schema)
                               (list (s/explain schema) (str "item " index))
                               (list (s/explain (s/eq schema)) (str "item " index))))
                           schemas))))))

(defn contains-items [schemas]
  (in-any-order schemas :extras-ok true))

(defn in-order [schemas]
  (reify s/Schema
    (walker [this]
      (let [item-schemas (build-schemas schemas)
            err-conj (u/result-builder (constantly []))]
        (fn [xs]
          (if-not (seqable? xs)
            (m/validation-error this xs (list 'seq? xs))
            (loop [xs xs
                   item-schemas item-schemas
                   out []]
              (if (empty? item-schemas)

                (if (empty? xs)
                  ; no remaining schemas, no remaining items
                  out

                  ; more items than schemas
                  (err-conj out (m/validation-error nil xs (list 'has-extra-elts? xs))))

                (if (empty? xs)
                  ; more schemas than items
                  (err-conj out
                            (m/validation-error
                              (vec (map :schema item-schemas))
                              nil
                              (list* 'missing-items? (map :schema item-schemas))))

                  (let [x (first xs)
                        item-schema (first item-schemas)]
                    (recur (rest xs)
                           (rest item-schemas)
                           (err-conj out ((:walker item-schema) x)))))))))))

    (explain [this]
      (list 'in-order
            (doall
              (map-indexed (fn [schema index]
                             (if (satisfies? s/Schema schema)
                               (list (s/explain schema) (str "item " index))
                               (list (s/explain (s/eq schema)) (str "item " index))))
                           schemas))))))

(deftype SortedSchema [comparator schema]
  s/Schema
  (walker [this]
    (let [w (s/subschema-walker schema)]
      (fn [value]
        (w (sort-by comparator value)))))
  (explain [this]
    (s/explain schema)))

(defn when-sorted
  ([schema]
   (when-sorted identity schema))
  ([comparator schema]
     (SortedSchema. comparator schema)))

(defn always-a-seq [s]
  (with-coercion #(if (sequential? %) % [%]) [s]))
