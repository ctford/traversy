(defproject traversy "0.2.1-SNAPSHOT"
  :description "Multilenses for Clojure."
  :url "https://github.com/ctford/traversy"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.typed "0.2.72"]]
  :core.typed {:check [traversy.lens]}
  :profiles {:dev
             {:plugins [[lein-midje "3.1.3"]
                        [lein-typed "0.3.5"]]
              :dependencies [[midje "1.6.3"]]}})
