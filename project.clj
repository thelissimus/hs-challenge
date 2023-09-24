(defproject challenge "0.1.0-SNAPSHOT"
  :description "Health Samurai Challenge"
  :license {:name "BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [http-kit/http-kit "2.7.0"]
                 [ring/ring-core "1.10.0"]
                 [ring/ring-devel "1.10.0"]
                 [compojure "1.7.0"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.zaxxer/HikariCP "5.0.1"]
                 [org.postgresql/postgresql "42.6.0"]
                 [com.taoensso/timbre "6.2.2"]]
  :main ^:skip-aot challenge.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
