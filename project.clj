(defproject elo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "https://github.com/andreacrotti/el"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.4"]
                 [clj-http "3.9.0"]
                 [ring "1.6.3"]
                 [compojure "1.6.1"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [org.postgresql/postgresql "42.2.2"]
                 [environ/environ.core "0.3.1"]
                 [hiccup "1.0.5"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.3.2"]
                 [org.clojure/tools.cli "0.3.5"]
                 [honeysql "0.9.1"]
                 [environ "1.1.0"]
                 [garden "1.3.5"]
                 [buddy "2.0.0"]
                 [buddy/buddy-auth "2.1.0"]
                 [migratus "1.0.6"]]

  :plugins [[environ/environ.lein "0.3.1"]
            [lein-ring "0.9.7"]
            [lein-cljfmt "0.5.7"]
            [lein-garden "0.2.8"]]

  :main ^{:skip-aot true} elo.api

  :uberjar-name "elo.jar"
  :min-lein-version "2.7.1"
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :ring {:handler elo.api/app}

  :migratus {:store :database
             :migration-dir "migrations"
             ;; can use environ here??
             :db ~(get (System/getenv) "DATABASE_URL")}

  :garden {:builds [{:id "screen"
                     :source-paths ["src/clj" "src/cljc"]
                     :stylesheet elo.css/screen
                     :compiler {:output-to "resources/public/css/screen.css"
                                :pretty-print? true}}]}
  )
