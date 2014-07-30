# vice

Useful extensions to [Prismatic](https://github.com/Prismatic/schema).

 - `vice.coercions` add coercions to individual schema using `(with-coercion func schema)`. Validate using `vice.coercions/validate`
 - `vice.valuetypes` common schemas for dates, numbers, etc. including coercions from strings. Useful for transforming json.
 - `vice.schemas` contains `between`, `conditional-on-key`, `fail`, `is-not`, `in-any-order`, `in-order`
 - `vice.midje` use schemas as [Midje](https://github.com/marick/Midje) checkers. `fail`, `is-not`, `in-any-order`, `in-order` are useful here.

## Latest version

See [clojars](https://clojars.org/repo/savagematt/vice)

## Usage

### Add coercions to schema:

```clj
(vice.coerce/validate {:a 123} {:a (vice.coerce/with-coercion str s/Str)})
```

### Midje checkers. 

Using schemas for midje checks provides better error messages and flexibility in assertions.

Non-schema values in maps are treated as `(schema.core/eq x)`

```clj
(fact ""
   {:a 123}
   => (vice.midje/matches {:a 123})
```

`in-order` and `in-any-order` help with assertions:

```clj
(fact ""
   [{:a 123} {:a 345} {:a 234}] 
   => (vice.midje/matches (vice.schemas/in-any-order [{:a 123} {:a 234}] :extras-ok true)))
```


Map matching is loose by default:

```clj
{:a 123 :not-in-schema 234} => (vice.midje/matches {:a 123})

```

Strictness can be turned on and is scoped:

```clj
{:a {:b 123
     :not-in-schema 234}} 
=> (vice.midje/matches (vice.midje/strict {:a {:b Long}}))
; checker will fail because strictness is inherited when checking value of :a
```

### Common types with coercions

See `vice.schemas` for full list.

```clj
(vice.coerce/validate {:a "2014-07-29"} {:a JodaDateMidnight})

; NB: UK date format only
(vice.coerce/validate {:a "29/07/2014"} {:a JodaDateMidnight})

(vice.coerce/validate {:a "123"} {:a PositiveInteger})

(vice.coerce/validate {} {(s/optional-key :a) GenUuid}) ; => {:a eafa7062-7bb3-4b60-b1ea-ada2dbd283c8}
```

## License

Copyright (C) 2014 Matt Savage. Distributed under the Eclipse Public License, the same as Clojure.
