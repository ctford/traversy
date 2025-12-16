(defproject traversy "0.5.1"
            :description "Multilenses for Clojure."
            :url "https://github.com/ctford/traversy"
            :min-lein-version "2.1.2"
            :license {:name "MIT"
                      :url  "http://opensource.org/licenses/MIT"}
            :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                           [org.clojure/clojurescript "1.9.229" :scope "provided"]]
            :profiles {:dev {:plugins    [[com.jakemccrary/lein-test-refresh "0.10.0"]
                                          [codox "0.8.10"]
                                          [lein-cljsbuild "1.1.4"]
                                          [lein-doo "0.1.7"]]
                             :dependencies [[smidjen "0.2.1"]]}}
            :codox {:src-dir-uri "http://github.com/ctford/traversy/blob/0.3.0/"}
            :cljsbuild {:builds        {:test {:source-paths ["src" "test"]
                                               :compiler     {:output-to     "target/cljs/testable.js"
                                                              :main          traversy.test-runner
                                                              :target        :nodejs
                                                              :optimizations :none}}}}
            :aliases {"test-clj" ["test" "traversy.test.lens"] ;; Travis version of lein doesn't support reader conditionals yet
                      "test-cljs" ["doo" "node" "test" "once"]
                      "auto-cljs" ["doo" "node" "test" "auto"]
                      "test-all" ["do" "test-clj," "test-cljs"]})
