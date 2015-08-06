(defproject savagematt/vice "0.10"

  :description "Prismatic schema extensions"

  :url "http://github.com/savagematt/vice"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-time "0.6.0"]
                 [prismatic/schema "0.4.3"]]

  :profiles {:dev {:dependencies [[midje "1.7.0"]
                                  [midje-junit-formatter "0.1.0-SNAPSHOT"]]
                   :plugins [[lein-midje "3.1.3"]]}})
