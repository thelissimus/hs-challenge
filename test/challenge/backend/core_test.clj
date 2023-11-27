(ns challenge.backend.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing run-tests use-fixtures]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [cheshire.core :as json]
   [org.httpkit.server :refer [run-server]]
   [org.httpkit.client :refer [request]]
   [next.jdbc :refer [execute!]]
   [next.jdbc.connection :refer [->pool]]
   [challenge.backend.server :refer [app]]
   [challenge.backend.domain :as domain])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def ds (atom nil))
(def port 8080)
(def url (format "http://localhost:%d" port))

(deftest patients-get-all
  (testing "Empty result"
    (is (= (json/parse-string-strict (:body @(request {:url (str url "/patients") :method :get})) true)
           {:data [] :count 0})))

  (testing "Non-empty result"
    (let [data (map (fn [_] (gen/generate (s/gen ::domain/patient))) (range 10))]
      (is (= {:data data :count (count data)}
             {:data data :count (count data)})))))

(defn setup-server [tests]
  (let [datasource (->pool HikariDataSource
                           {:dbtype "postgresql"
                            :dbname "challenge_test"
                            :host "localhost"
                            :username "postgres"})
        server (run-server (app datasource) {:port 8080})]
    (reset! ds datasource)
    (tests)
    (server)
    (.close @ds)))

(defn reset-database [tests]
  (execute! @ds [(slurp (io/resource "schema.sql"))])
  (tests)
  (execute! @ds [(slurp (io/resource "down.sql"))]))

(use-fixtures :once setup-server)
(use-fixtures :each reset-database)

(run-tests)
