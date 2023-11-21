(defproject challenge "0.1.0-SNAPSHOT"
  :description "Health Samurai Challenge"
  :license {:name "BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [http-kit/http-kit "2.7.0"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-devel "1.10.0"]
                 [jumblerg/ring-cors "3.0.0"]
                 [compojure "1.7.0"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.zaxxer/HikariCP "5.0.1"]
                 [org.postgresql/postgresql "42.6.0"]
                 [org.slf4j/slf4j-api "2.0.9"]
                 [org.slf4j/slf4j-simple "2.0.9"]
                 [com.taoensso/timbre "6.2.2"]
                 [clojure.java-time "1.3.0"]
                 [cheshire "5.12.0"]
                 ;; frontend
                 [reagent "1.2.0"]
                 [re-frame "1.3.0"]
                 [superstructor/re-frame-fetch-fx "0.4.0"]
                 [metosin/reitit-spec "0.7.0-alpha7"]
                 [metosin/reitit-frontend "0.7.0-alpha7"]
                 [day8.re-frame/tracing "0.6.2"]
                 [binaryage/devtools "1.0.6"]
                 [day8.re-frame/re-frame-10x "1.5.0"]
                 ;; tooling
                 [thheller/shadow-cljs "2.26.0"]]
  :main ^:skip-aot challenge.backend.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
