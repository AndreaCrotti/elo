(defproject byf "0.1.0-SNAPSHOT"
  :description "Manage leagues of players uisng the Elo algorithm"
  :url "http://github.com/AndreaCrotti/elo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [clj-http "3.9.1"]
                 [prone "1.6.1"]
                 [integrant "0.7.0"]
                 [integrant/repl "0.3.1"]
                 [com.bhauman/figwheel-main "0.2.0"]
                 ;; server side libs
                 [org.clojure/data.csv "0.1.4"]
                 [aero "1.1.3"]
                 [ring "1.7.0"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [org.postgresql/postgresql "42.2.5"]
                 [nilenso/honeysql-postgres "0.2.5"]
                 [honeysql "0.9.4"]

                 [environ/environ.core "0.3.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-json "0.4.0"]
                 [bk/ring-gzip "0.3.0"]
                 [ring-oauth2 "0.1.4"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-mock "0.3.2"]

                 [buddy "2.0.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [migratus "1.0.9"]
                 ;;TODO: move these to the test profile
                 [org.clojure/test.check "0.9.0"]
                 [junit/junit "4.12"]

                 ;; [org.clojure/clojurescript "1.10.439"]
                 [org.clojure/clojurescript "1.10.520"]
                 [re-frame "0.10.6"]
                 [reagent-utils "0.3.2"]
                 [org.webjars/font-awesome "5.7.2"]

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

                 [bidi "2.1.5"]
                 [com.cemerick/url "0.1.1"]
                 [venantius/accountant "0.2.4"]
                 [medley "1.1.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [datascript "0.18.2"]
                 [aysylu/loom "1.0.2"]
                 [reifyhealth/specmonstah "2.0.0-alpha-1"]
                 [com.taoensso/timbre "4.10.0" :exclusions [com.taoensso/encore]]
                 [http-kit "2.3.0"]
                 [com.taoensso/sente "1.14.0-RC2"]]

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
  :resource-paths ["config" "resources"]

  ;; :main ^:skip-aot datomic-app.core
  :migratus {:store :database
             :migration-dir "migrations"
             ;; can use environ here??
             :db ~(get (System/getenv) "DATABASE_URL")}

  :aliases {"fig" ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev"]
            "cljs-prod" ["run" "-m" "figwheel.main" "--build-once" "prod"]
            "test-cljs" ["doo" "rhino" "test" "once"]
            "kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]}

  :cljfmt {:indents {for-all [[:block 1]]
                     fdef [[:block 1]]
                     checking [[:inner 0]]}}

  :profiles
  {:kaocha {:dependencies [[lambdaisland/kaocha "0.0-389"]
                           [lambdaisland/kaocha-cloverage "0.0-22"]
                           [lambdaisland/kaocha-junit-xml "0.0-63"]]}
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

    :dependencies [[binaryage/devtools "0.9.10"]
                   [cider/piggieback "0.4.0"]
                   [org.mozilla/rhino "1.7.7.1"]
                   [day8.re-frame/re-frame-10x "0.3.7"]
                   [day8.re-frame/tracing "0.5.1"]
                   [com.bhauman/rebel-readline-cljs "0.1.4"]
                   [ring/ring-mock "0.3.2"]]}}
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
