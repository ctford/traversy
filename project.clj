(defproject traversy "0.1.0-SNAPSHOT"
  :description "Lenses and multilenses."
  :url "https://github.com/ctford/traversy"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev
             {:plugins [[lein-midje "3.1.3"]]
              :dependencies [[midje "1.6.3"]]}})
