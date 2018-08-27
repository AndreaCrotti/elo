(defproject elo "0.1.0-SNAPSHOT"
  :description "Compute Fifa players Elo score"
  :url "http://github.com/AndreaCrotti/elo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/spec.alpha "0.2.168"]
                 [clj-http "3.9.1"]
                 [ring "1.6.3"]
                 [compojure "1.6.1"]
                 [org.clojure/java.jdbc "0.7.8"]
                 [org.postgresql/postgresql "42.2.4"]
                 [nilenso/honeysql-postgres "0.2.4"]
                 [environ/environ.core "0.3.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-mock "0.3.2"]
                 [org.clojure/tools.cli "0.3.7"]
                 [honeysql "0.9.3"]
                 [environ "1.1.0"]
                 [garden "1.3.5"]
                 [buddy "2.0.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [migratus "1.0.8"]
                 [org.clojure/test.check "0.9.0"]

                 [org.clojure/clojurescript "1.10.339"]
                 [cljs-react-material-ui "0.2.48"]
                 [re-frame "0.10.5"]
                 [reagent-forms "0.5.42"]

                 [cljsjs/react-datepicker "1.5.0-0"]
                 [cljsjs/classnames "2.2.5-1"]
                 [cljsjs/prop-types "15.6.1-0"]
                 [cljsjs/react-onclickoutside "6.7.1-1"]
                 [cljsjs/react-popper "0.10.4-0"]
                 [cljsjs/popperjs "1.14.3-1"]

                 [ns-tracker "0.3.1"]
                 [cljsjs/moment "2.22.2-0"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [cljs-ajax "0.7.4"]
                 [cljs-http "0.1.45"]
                 [buddy/buddy-auth "2.1.0"]
                 [buddy "2.0.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [re-com "2.1.0"]
                 [bidi "2.1.3"]
                 [com.cemerick/url "0.1.1"]]

  :plugins [[environ/environ.lein "0.3.1"]
            [migratus-lein "0.5.0"]
            [lein-cljsbuild "1.1.4"]
            [lein-ring "0.9.7"]
            [lein-cljfmt "0.5.7"]
            [lein-garden "0.2.8"]]

  :main elo.api
  :aot [elo.api]

  :uberjar-name "elo.jar"
  :min-lein-version "2.7.1"
  :source-paths ["src/cljc" "src/clj"]
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


  :profiles
  {:production {:env {:production true}}
   :uberjar {:hooks []
             :source-paths ["src/clj" "src/cljc"]
             :prep-tasks [["compile"]
                          ["garden" "once"]
                          ["cljsbuild" "once" "min"]]

             :omit-source true
             :aot :all
             :main elo.api}

   :dev
   {:repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
    :figwheel {:css-dirs ["resources/public/css"]
               :ring-handler elo.api/app
               :server-logfile "log/figwheel.log"
               :server-ip "127.0.0.1"
               :server-port 3452}

    :plugins [[lein-figwheel "0.5.16"]
              [lein-doo "0.1.10"]
              [migratus-lein "0.5.0"]]

    :dependencies [[binaryage/devtools "0.9.10"]
                   [com.cemerick/piggieback "0.2.2"]
                   [figwheel "0.5.16"]
                   [figwheel-sidecar "0.5.16"]
                   [day8.re-frame/re-frame-10x "0.3.3"]
                   ;; dependencies for the reloaded workflow
                   [reloaded.repl "0.2.4"]
                   [ring/ring-mock "0.3.2"]]}}
  :cljsbuild
  {:builds
   [{:id "test"
     :source-paths ["src/cljs" "test/cljs" "src/cljc" "test/cljc"]
     :compiler {:output-to "resources/public/js/testable.js"
                :main elo.test-runner
                :optimizations :none}}

    {:id "dev"
     :source-paths ["src/cljs" "src/cljc"]
     :figwheel     {:on-jsload "elo.core/mount-root"}
     :compiler     {:main elo.core
                    :output-to "resources/public/js/compiled/app.js"
                    :output-dir "resources/public/js/compiled/out"
                    :asset-path "js/compiled/out"
                    :optimizations :none
                    :source-map true
                    :source-map-timestamp true
                    :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                    :preloads [devtools.preload day8.re-frame-10x.preload]
                    :external-config {:devtools/config {:features-to-install [:formatters
                                                                              :async
                                                                              :hints]}}}}
    {:id "min"
     :source-paths ["src/cljs" "src/cljc"]
     :compiler     {:main elo.core
                    :output-to "resources/public/js/compiled/app.js"
                    :optimizations :advanced
                    :output-dir "resources/public/js/compiled"
                    :source-map "resources/public/js/compiled/app.js.map"
                    :closure-defines {goog.DEBUG false}
                    :pretty-print false}}]})
