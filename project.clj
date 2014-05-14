(defproject matross/mapstache "0.2.0-SNAPSHOT"
  :description "More expressive end user configs via templated map values"
  :url "https://github.com/matross/mapstache"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :scm {:name "git"
        :url "https://github.com/matross/mapstache"}
  :pom-addition [:developers [:developer {:id "zeroem"}
                              [:name "Darrell Hamilton"]
                              [:url "https://github.com/zeroem"]]]
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev {:dependencies [[de.ubercode.clostache/clostache "1.4.0"]]}}
  :plugins [[codox "0.8.0"]])
