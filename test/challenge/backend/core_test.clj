(ns challenge.backend.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [cheshire.core :as json]
   [org.httpkit.server :refer [run-server]]
   [org.httpkit.client :refer [request]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.connection :refer [->pool]]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
   [next.jdbc.date-time :refer [read-as-local]]
   [challenge.backend.lib :refer [parse-json]]
   [challenge.backend.server :as server]
   [challenge.backend.domain :as domain])
  (:import (com.zaxxer.hikari HikariDataSource)))

;;; constants

(def datasource (atom nil))
(def db-spec {:dbtype "postgresql"
              :dbname "challenge_test"
              :host "localhost"
              :username "postgres"})
(def port 8080)
(def url (format "http://localhost:%d" port))
(def url-patients (str url "/patients"))
(defn url-patient [id] (str url-patients "/" id))

;;; utils

(defn gen-patient []
  (gen/generate (s/gen ::domain/patient)))

(defn ds-conf [ds]
  (jdbc/with-options ds {:builder-fn as-unqualified-lower-maps}))

;;; fixtures

(defn setup-server [tests]
  (with-open [^HikariDataSource ds (->pool HikariDataSource db-spec)]
    (let [server (run-server (server/app ds) {:port 8080})]
      (read-as-local)
      (reset! datasource ds)
      (tests)
      (server))))

(defn reset-database [tests]
  (jdbc/execute! @datasource [(slurp (io/resource "schema.sql"))])
  (tests)
  (jdbc/execute! @datasource [(slurp (io/resource "down.sql"))]))

(use-fixtures :once setup-server)
(use-fixtures :each reset-database)

;;; tests

;; GET /patients
(deftest patients-get-all
  (testing "Empty result"
    (is (= (parse-json (:body @(request {:url url-patients :method :get})))
           {:data [] :count 0})))

  (testing "Non-empty result"
    (let [patients (repeat 5 (gen-patient))
          entities (map #(s/conform ::domain/patient %) patients)
          enumerated (map-indexed (fn [i a] (merge a {:id (inc i)})) patients)]
      (sql/insert-multi! @datasource :patients (-> entities first keys vec) (vec (map vals entities)))

      (is (= (parse-json (:body @(request {:url url-patients :method :get})))
             {:data enumerated :count (count enumerated)})))))

;; POST /patients
(deftest patients-create
  (testing "Create patient successfully with valid patient data"
    (let [patient (gen-patient)]
      @(request {:url url-patients
                 :method :post
                 :body (json/generate-string patient)})

      (is (= (update (jdbc/execute-one! (ds-conf @datasource) ["SELECT * FROM patients WHERE id = 1;"]) :birth_date #(.toString %))
             (merge patient {:id 1}))))))

(deftest patients-create-response
  (testing "Return created patient"
    (let [patient (gen-patient)
          response @(request {:url url-patients
                              :method :post
                              :body (json/generate-string patient)})]
      (is (= (parse-json (:body response))
             (merge patient {:id 1}))))))

(deftest patients-create-invalid
  (testing "Return a proper error response for invalid data"
    (let [invalid (dissoc (gen-patient) :first_name)
          response @(request {:url url-patients
                              :method :post
                              :body (json/generate-string invalid)})]

      (is (= (:status response) 400))
      (is (s/valid? ::server/server-error (parse-json (:body response)))))))

;; GET /patients/:id
(deftest patients-get
  (testing "204 for non existent user"
    (is (= (:status @(request {:url (url-patient 1) :method :get}))
           204)))

  (testing "Getting user data for the existing user"
    (let [patient (gen-patient)]
      (sql/insert! @datasource :patients (s/conform ::domain/patient patient))

      (is (= (parse-json (:body @(request {:url (url-patient 1) :method :get})))
             (merge patient {:id 1}))))))

;; DELETE /patients/:id
(deftest patients-delete
  (testing "404 when trying to delete non existent patient"
    (let [response @(request {:url (url-patient 1) :method :delete})]
      (is (= (:status response) 404))
      (is (empty? (:body response)))))

  (testing "204 when trying to delete an existing patient"
    (sql/insert! @datasource :patients (s/conform ::domain/patient (gen-patient)))

    (let [response @(request {:url (url-patient 1) :method :delete})]
      (is (= (:status response) 204))
      (is (empty? (:body response))))))
