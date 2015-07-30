(defproject traversy "0.3.2-SNAPSHOT"
            :description "Multilenses for Clojure."
            :url "https://github.com/ctford/traversy"
            :min-lein-version "2.1.2"
            :license {:name "MIT"
                      :url  "http://opensource.org/licenses/MIT"}
            :dependencies [[org.clojure/clojure "1.7.0"]
                           [org.clojure/clojurescript "0.0-3308"]]
            :profiles {:dev {:plugins    [[com.jakemccrary/lein-test-refresh "0.10.0"]
                                          [codox "0.8.10"]
                                          [lein-cljsbuild "1.0.6"]
                                          [com.cemerick/clojurescript.test "0.3.3"]]}}
            :codox {:src-dir-uri "http://github.com/ctford/traversy/blob/0.3.0/"}
            :cljsbuild {:builds        {:test {:source-paths ["src" "test"]
                                               :compiler     {:output-to     "target/cljs/testable.js"
                                                              :optimizations :whitespace}}}
                        :test-commands {"unit-tests" ["phantomjs" :runner
                                                      "window.literal_js_was_evaluated=true"
                                                      "target/cljs/testable.js"]}}
            :aliases {"test-clj" ["test" "traversy.test.lens"] ;; lein test doesn't pick up cljc yet
                      "test-cljs" ["cljsbuild" "test"]
                      "test-all" ["do" "test-clj," "test-cljs"]}
            )
