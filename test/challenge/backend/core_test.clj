(ns challenge.backend.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [aero.core :refer [read-config]]
   [cheshire.core :as json]
   [org.httpkit.server :refer [run-server]]
   [org.httpkit.client :refer [request]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.connection :refer [->pool]]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
   [next.jdbc.date-time :refer [read-as-local]]
   [mount.core :refer [defstate] :as mount]
   [challenge.backend.lib :refer [parse-json]]
   [challenge.backend.config :as config]
   [challenge.backend.server :as server]
   [challenge.backend.domain :as domain])
  (:import (com.zaxxer.hikari HikariDataSource)))

;;; State
(defstate configuration
  :start (-> (io/resource "config/backend.edn")
             (read-config {:profile :test})
             (#(s/conform ::config/config %))))

(defstate datasource
  :start ^HikariDataSource (->pool HikariDataSource (:db configuration))
  :stop (.close datasource))

(defstate url-patients :start (format "http://localhost:%d/%s" (:port configuration) "patients"))
(defn url-patient [id] (str url-patients "/" id))

;;; Utils
(defn gen-patient []
  (gen/generate (s/gen ::domain/patient)))

(defn gen-patient-entity []
  (s/conform ::domain/patient (gen-patient)))

(defn ds-conf [ds]
  (jdbc/with-options ds {:builder-fn as-unqualified-lower-maps}))

;;; Fixtures
(defn setup-server [tests]
  (mount/start)
  (let [server (run-server (server/app datasource) {:port (:port configuration)})]
    (read-as-local)
    (tests)
    (server))
  (mount/stop))

(defn reset-database [tests]
  (jdbc/execute! datasource [(slurp (io/resource "schema/schema.sql"))])
  (tests)
  (jdbc/execute! datasource [(slurp (io/resource "schema/down.sql"))]))

(use-fixtures :once setup-server)
(use-fixtures :each reset-database)

;;; Tests
;; GET /patients
(deftest patients-get-all
  (testing "Empty result"
    (is (= (parse-json (:body @(request {:url url-patients :method :get})))
           {:data [] :count 0})))

  (testing "Get all properly"
    (let [patients (repeat 5 (gen-patient))
          entities (map #(s/conform ::domain/patient %) patients)
          enumerated (map-indexed (fn [i a] (merge a {:id (inc i)})) patients)]
      (sql/insert-multi! datasource :patients (-> entities first keys vec) (vec (map vals entities)))
      (is (= (parse-json (:body @(request {:url url-patients :method :get})))
             {:data enumerated :count (count enumerated)})))))

;; POST /patients
(deftest patients-create
  (testing "Create properly"
    (let [patient (gen-patient)]
      @(request {:url url-patients
                 :method :post
                 :body (json/generate-string patient)})
      (is (= (update (jdbc/execute-one! (ds-conf datasource) ["SELECT * FROM patients WHERE id = 1;"]) :birth_date #(.toString %))
             (merge patient {:id 1}))))))

(deftest patients-create-response
  (testing "Return created"
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
  (testing "404 when non existent"
    (is (= (:status @(request {:url (url-patient 1) :method :get})) 404)))

  (testing "Get one properly"
    (let [patient (gen-patient)]
      (sql/insert! datasource :patients (s/conform ::domain/patient patient))
      (is (= (parse-json (:body @(request {:url (url-patient 1) :method :get})))
             (merge patient {:id 1}))))))

;; PATCH /patients/:id
(deftest patients-update
  (testing "404 when attempting to update non-existent entry"
    (let [response @(request {:url (url-patient 1)
                              :method :patch
                              :body (json/generate-string (gen-patient))})]
      (is (= (:status response) 404))
      (is (empty? (:body response)))))

  (testing "Update properly"
    (sql/insert! datasource :patients (gen-patient-entity))
    (let [response @(request {:url (url-patient 1)
                              :method :patch
                              :body (json/generate-string (gen-patient))})]
      (is (= (:status response) 204))
      (is (empty? (:body response)))))

  (testing "400 when attempting to update with invalid data"
    (let [response @(request {:url (url-patient 1)
                              :method :patch
                              :body (json/generate-string {:meaning_of_life 42})})]
      (is (= (:status response) 400))
      (is (s/valid? ::server/server-error (parse-json (:body response))))))

  (testing "400 when attempting to update with empty data"
    (let [response @(request {:url (url-patient 1)
                              :method :patch
                              :body "{}"})]
      (is (= (:status response) 400))
      (is (s/valid? ::server/server-error (parse-json (:body response)))))))

;; DELETE /patients/:id
(deftest patients-delete
  (testing "404 when attempting to delete non-existent entry"
    (let [response @(request {:url (url-patient 1) :method :delete})]
      (is (= (:status response) 404))
      (is (empty? (:body response)))))

  (testing "Delete properly"
    (sql/insert! datasource :patients (gen-patient-entity))
    (let [response @(request {:url (url-patient 1) :method :delete})]
      (is (= (:status response) 204))
      (is (empty? (:body response))))))
