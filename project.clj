(defproject challenge "0.1.0-SNAPSHOT"
  :description "Health Samurai Challenge"
  :license {:name "BSD-3-Clause"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot challenge.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
