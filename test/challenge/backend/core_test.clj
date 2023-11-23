(ns challenge.backend.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing run-tests use-fixtures]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [next.jdbc :refer [execute!]]
   [next.jdbc.connection :refer [->pool]]
   [challenge.backend.domain :as domain])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def ds (atom nil))

(deftest patients-get-all
  (testing "Empty result"
    (is (= {:data [] :count 0}
           {:data [] :count 0})))

  (testing "Non-empty result"
    (let [data (map (fn [_] (gen/generate (s/gen ::domain/patient))) (range 10))]
      (is (= {:data data :count (count data)}
             {:data data :count (count data)})))))

(defn setup-database [tests]
  (reset! ds (->pool HikariDataSource
                     {:dbtype "postgresql"
                      :dbname "challenge_test"
                      :host "localhost"
                      :username "postgres"}))
  (tests)
  (.close @ds))

(defn reset-database [tests]
  (execute! @ds [(slurp (io/resource "schema.sql"))])
  (tests)
  (execute! @ds [(slurp (io/resource "down.sql"))]))

(use-fixtures :once setup-database)
(use-fixtures :each reset-database)

(run-tests)
