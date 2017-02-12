(defproject traversy "0.5.0"
            :description "Multilenses for Clojure."
            :url "https://github.com/ctford/traversy"
            :min-lein-version "2.1.2"
            :license {:name "MIT"
                      :url  "http://opensource.org/licenses/MIT"}
            :dependencies [[org.clojure/clojure "1.8.0"]
                           [org.clojure/clojurescript "1.9.229"]]
            :profiles {:dev {:plugins    [[com.jakemccrary/lein-test-refresh "0.10.0"]
                                          [codox "0.8.10"]
                                          [lein-cljsbuild "1.1.4"]
                                          [lein-doo "0.1.7"]]
                             :dependencies [[smidjen "0.2.1"]]}}
            :codox {:src-dir-uri "http://github.com/ctford/traversy/blob/0.3.0/"}
            :cljsbuild {:builds        {:test {:source-paths ["src" "test"]
                                               :compiler     {:output-to     "target/cljs/testable.js"
                                                              :main          traversy.test-runner
                                                              :optimizations :whitespace}}}}
            :aliases {"test-cljs" ["doo" "phantom" "test" "once"]
                      "auto-cljs" ["doo" "phantom" "test" "auto"]
                      "test-all" ["do" "test," "test-cljs"]}
            )
