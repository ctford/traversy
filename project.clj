(defproject traversy "0.2.0"
  :description "Multilenses for Clojure."
  :url "https://github.com/ctford/traversy"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev
             {:plugins [[lein-midje "3.1.3"]]
              :dependencies [[midje "1.6.3"]]}})
