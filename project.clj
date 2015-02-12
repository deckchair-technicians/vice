(defproject savagematt/vice "0.3"

  :description "Prismatic schema extensions"

  :url "http://github.com/savagematt/vice"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-time "0.6.0"]
                 [prismatic/schema "0.2.5"]]

  :profiles {:dev {:dependencies [[midje "1.6.2"]
                                  [midje-junit-formatter "0.1.0-SNAPSHOT"]]
                   :plugins [[lein-midje "3.1.0"]]}})
