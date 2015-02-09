(defproject traversy "0.3.1"
  :description "Multilenses for Clojure."
  :url "https://github.com/ctford/traversy"
  :license {:name "MIT"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :profiles {:dev
             {:plugins [[lein-midje "3.1.3"]
                        [codox "0.8.10"]]
              :dependencies [[midje "1.6.3"]]}}
  :codox {:src-dir-uri "http://github.com/ctford/traversy/blob/0.3.0/"})
