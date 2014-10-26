(defproject matross/mapstache "0.3.2"
  :description "More expressive end user configs via templated map values"
  :url "https://github.com/matross/mapstache"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :scm {:name "git"
        :url "https://github.com/matross/mapstache"}
  :pom-addition [:developers [:developer
                              [:id "zeroem"]
                              [:name "Darrell Hamilton"]
                              [:url "https://github.com/zeroem"]]
                 [:developer
                  [:id "eggsby"]
                  [:name "Thomas Omans"]
                  [:url "https://github.com/eggsby"]]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [potemkin  "0.3.4"]]
  :profiles {:dev {:dependencies [[stencil "0.3.3"]]}}
  :plugins [[codox "0.8.0"]])
