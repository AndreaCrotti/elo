(defproject byf "0.1.0-SNAPSHOT"
  :description "Manage leagues of players uisng the Elo algorithm"
  :url "http://github.com/AndreaCrotti/elo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.reader "1.5.0"]
                 [org.clojure/spec.alpha "0.5.238"]
                 [clj-http "3.13.0"]
                 [clj-time "0.15.2"]
                 [prone "2021-04-23"]
                 [integrant "0.13.0"]
                 [integrant/repl "0.3.3"]
                 [com.bhauman/figwheel-main "0.2.18"]
                 ;; server side libs
                 [org.clojure/data.csv "1.1.0"]
                 [aero "1.1.6"]
                 [ring "1.10.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.7.4"]
                 [nilenso/honeysql-postgres "0.4.112"]
                 [honeysql "1.0.461"]

                 [environ/environ.core "0.3.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring/ring-json "0.5.1"]
                 [bk/ring-gzip "0.3.0"]
                 [ring-oauth2 "0.3.0"]
                 [ring/ring-defaults "0.5.0"]
                 [ring/ring-mock "0.4.0"]

                 [buddy "2.0.0"]
                 [buddy/buddy-auth "3.0.323"]
                 [migratus "1.6.0"]
                 ;;TODO: move these to the test profile
                 [org.clojure/test.check "1.1.1"]
                 [junit/junit "4.13.2"]

                 ;; [org.clojure/clojurescript "1.10.439"]
                 [org.clojure/clojurescript "1.10.520"]
                 [re-frame "1.4.3"]
                 [com.degel/re-frame-firebase "0.8.0"]
                 [reagent-utils "0.3.8"]
                 [org.webjars/font-awesome "6.5.2"]
                 [antizer "0.3.3"]

                 [day8.re-frame/tracing-stubs "0.6.2"]
                 [expound "0.9.0"]
                 [cljsjs/react "18.2.0-1"]
                 [cljsjs/react-dom "18.2.0-1"]
                 ;; [metasoarous/oz "1.3.1"]
                 [cljsjs/vega-lite "5.14.1-0"]
                 [cljsjs/vega "5.25.0-0"]

                 [ns-tracker "1.0.0"]
                 [cljsjs/moment "2.29.4-0"]
                 [day8.re-frame/http-fx "0.2.4"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljs-ajax "0.8.4"]
                 [cljs-http "0.1.48"]
                 [ring-cljsjs "0.2.0"]

                 [lambdaisland/uri "1.19.155"]
                 [bidi "2.1.6"]
                 [com.cemerick/url "0.1.1"]
                 [venantius/accountant "0.2.5"]
                 [medley "1.4.0"]
                 [metosin/ring-http-response "0.9.4"]
                 [datascript "1.7.3"]
                 [aysylu/loom "1.0.2"]
                 [reifyhealth/specmonstah "2.1.0"]
                 [com.taoensso/timbre "6.5.0"]
                 [http-kit "2.8.0"]
                 [com.taoensso/sente "1.19.2"]
                 [org.clojure/tools.cli "1.1.230"]]

  :plugins [[lein-environ "1.1.0"]
            [migratus-lein "0.5.0"]
            [lein-doo "0.1.6"]
            [lein-cljsbuild "1.1.4"]
            [jonase/eastwood "0.3.3"]
            [lein-ring "0.9.7"]
            [test2junit "1.3.3"]]

  ;; :pedantic? :warn

  :test2junit-output-dir ~(or (System/getenv "CIRCLE_TEST_REPORTS") "target/test2junit")

  :uberjar-name "byf.jar"
  :min-lein-version "2.8.1"
  :source-paths ["src/cljc" "src/clj" "src/cljs"]
  :test-paths ["test/clj" "test/cljc"]
  :ring {:handler byf.api/app}
  :resource-paths ["resources"]

  ;; :main ^:skip-aot datomic-app.core
  :migratus {:store :database
             :migration-dir "migrations"
             ;; can use environ here??
             :db ~(get (System/getenv) "DATABASE_URL")}

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev"]
            "cljs-prod" ["run" "-m" "figwheel.main" "--build-once" "prod"]
            "test-cljs" ["doo" "rhino" "test" "once"]
            "kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]
            "seed" ["run" "-m" "byf.seed"]}

  :cljfmt {:indents {for-all [[:block 1]]
                     fdef [[:block 1]]
                     checking [[:inner 0]]}}

  :profiles
  {:kaocha {:dependencies [[lambdaisland/kaocha "1.91.1392"]
                           [lambdaisland/kaocha-cloverage "1.1.89"]
                           [lambdaisland/kaocha-junit-xml "1.17.101"]]}
   :uberjar {:hooks []
             :source-paths ["src/clj" "src/cljc"]
             :prep-tasks [["compile"]
                          ["cljsbuild" "once" "min"]]
             :omit-source true
             :aot [byf.api]
             :main byf.api}

   :test
   {:env {:environment :test}}

   :dev
   {:ring {:stacktrace-middleware prone.middleware/wrap-exceptions}
    :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
    :source-paths ["src/cljc" "src/clj" "src/cljs" "dev"]
    :plugins [[migratus-lein "0.5.0"]
              [lein-cljfmt "0.5.7"]]

    :env {:environment :dev}

    :dependencies [[binaryage/devtools "1.0.7"]
                   [cider/piggieback "0.5.3"]
                   [org.mozilla/rhino "1.7.15"]
                   [day8.re-frame/re-frame-10x "1.9.9"]
                   [day8.re-frame/tracing "0.6.2"]
                   [com.bhauman/rebel-readline-cljs "0.1.4"]
                   [ring/ring-mock "0.4.0"]]}}
  :cljsbuild
  {:builds
   [{:id "test"
     :source-paths ["src/cljs" "src/cljc" "test/cljs" "test/cljc"]
     :compiler {:output-to "target/unit-test.js"
                :main 'byf.runner
                :optimizations :whitespace}}

    {:id "min"
     :source-paths ["src/cljs" "src/cljc"]
     :compiler     {:main byf.core
                    :output-to "resources/public/cljs-out/dev-main.js"
                    :asset-path "resources/public/cljs-out/byf"
                    :optimizations :advanced
                    :parallel-build true
                    :closure-defines {goog.DEBUG true}
                    :pretty-print true}}]})
