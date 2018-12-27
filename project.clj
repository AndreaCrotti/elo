(defproject elo "0.1.0-SNAPSHOT"
  :description "Compute Fifa players Elo score"
  :url "http://github.com/AndreaCrotti/elo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [clj-http "3.9.1"]
                 [prone "1.6.1"]

                 ;; server side libs
                 [org.clojure/data.csv "0.1.4"]
                 [ring "1.7.0"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.postgresql/postgresql "42.2.5"]
                 [nilenso/honeysql-postgres "0.2.4"]
                 [honeysql "0.9.4"]

                 [environ/environ.core "0.3.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-json "0.4.0"]
                 [ring-oauth2 "0.1.4"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-mock "0.3.2"]

                 [garden "1.3.6"]
                 [buddy "2.0.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [migratus "1.0.9"]
                 ;;TODO: move these to the test profile
                 [org.clojure/test.check "0.9.0"]
                 [junit/junit "4.12"]

                 [org.clojure/clojurescript "1.10.439"]
                 [re-frame "0.10.6"]
                 [org.webjars/font-awesome "5.5.0"]

                 [day8.re-frame/tracing-stubs "0.5.1"]
                 [expound "0.7.2"]
                 [cljsjs/react "16.6.0-0"]
                 [cljsjs/react-dom "16.6.0-0"]
                 ;; these below are all needed by react datepicker and
                 ;; everything blows up if they are not pinned apparently
                 [cljsjs/react-datepicker "1.5.0-0"]
                 [cljsjs/classnames "2.2.5-1"]
                 [cljsjs/prop-types "15.6.2-0"]
                 [cljsjs/react-onclickoutside "6.7.1-1"]
                 [cljsjs/react-popper "0.10.4-0"]
                 [cljsjs/popperjs "1.14.3-1"]
                 ;; [metasoarous/oz "1.3.1"]
                 [cljsjs/vega-lite "2.6.0-1"]
                 [cljsjs/vega "4.3.0-0"]

                 [ns-tracker "0.3.1"]
                 [cljsjs/moment "2.22.2-2"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljs-ajax "0.8.0"]
                 [cljs-http "0.1.45"]
                 [com.taoensso/timbre "4.10.0"]
                 [re-com "2.3.0"]
                 [bidi "2.1.4"]
                 [com.cemerick/url "0.1.1"]
                 [venantius/accountant "0.2.4"]
                 [medley "1.0.0"]
                 [metosin/ring-http-response "0.9.1"]]

  :plugins [[environ/environ.lein "0.3.1"]
            [migratus-lein "0.5.0"]
            [lein-cljsbuild "1.1.4"]
            [jonase/eastwood "0.3.3"]
            [lein-ring "0.9.7"]
            [test2junit "1.3.3"]
            [lein-garden "0.2.8"]]

  ;; :pedantic? :warn

  :test2junit-output-dir ~(or (System/getenv "CIRCLE_TEST_REPORTS") "target/test2junit")

  :main elo.api
  :aot [elo.api]

  :uberjar-name "elo.jar"
  :min-lein-version "2.7.1"
  :source-paths ["src/cljc" "src/clj" "src/cljs"]
  :test-paths ["test/clj" "test/cljc"]
  :ring {:handler elo.api/app}
  :resource-paths ["config" "resources"]

  :migratus {:store :database
             :migration-dir "migrations"
             ;; can use environ here??
             :db ~(get (System/getenv) "DATABASE_URL")}

  :garden {:builds [{:id "screen"
                     :source-paths ["src/clj" "src/cljc"]
                     :stylesheet elo.css/screen
                     :compiler {:output-to "resources/public/css/screen.css"
                                :pretty-print? true}}]}


  :aliases {"test-cljs" ["doo" "phantom" "test" "once"]
            "fig" ["trampoline" "run" "-m" "figwheel.main"]
            "build" ["trampoline" "run" "-m" "figwheel.main" "-b" "elo"]}

  :cljfmt {:indents {for-all [[:block 1]]
                     fdef [[:block 1]]
                     checking [[:inner 0]]}}

  :profiles
  {:production {:env {:production true}}
   :uberjar {:hooks []
             :source-paths ["src/clj" "src/cljc"]
             :prep-tasks [["compile"]
                          ["garden" "once"]
                          ["cljsbuild" "once" "min"]]

             :omit-source true
             :aot [elo.api]
             :main elo.api}

   :dev
   {:ring {:stacktrace-middleware prone.middleware/wrap-exceptions}
    :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
    :plugins [[lein-doo "0.1.10"]
              [migratus-lein "0.5.0"]
              [lein-cloverage "1.0.13"]
              [lein-cljfmt "0.5.7"]]

    :dependencies [[binaryage/devtools "0.9.10"]
                   [cider/piggieback "0.3.10"]
                   [venantius/yagni "0.1.6"]
                   [com.bhauman/figwheel-main "0.1.9"]
                   [day8.re-frame/re-frame-10x "0.3.5"]
                   [day8.re-frame/tracing "0.5.1"]
                   [com.bhauman/rebel-readline-cljs "0.1.4"]
                   ;; dependencies for the reloaded workflow
                   [reloaded.repl "0.2.4"]
                   [ring/ring-mock "0.3.2"]]}}
  :cljsbuild
  {:builds
   [{:id "min"
     :source-paths ["src/cljs" "src/cljc"]
     :compiler     {:main elo.core

                    :output-to "resources/public/cljs-out/elo-main.js"
                    :asset-path "resources/public/cljs-out/elo"
                    :optimizations :simple
                    :closure-defines {goog.DEBUG true}
                    :pretty-print true}}]})
