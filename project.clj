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
                 [cheshire "5.12.0"]]
  :main ^:skip-aot challenge.backend.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
