(ns challenge.backend.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [aero.core :refer [read-config]]
   [cheshire.core :as json]
   [lambdaisland.uri :refer [assoc-query* uri uri-str]]
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

(defstate url-patients :start (-> (uri "http://localhost")
                                  (assoc :port (:port configuration))
                                  (assoc :path "/patients")))

;;; Utils
(defn gen-patient []
  (gen/generate (s/gen ::domain/patient)))

(defn patient->entity [patient]
  (s/conform ::domain/patient patient))

(defn append-path [u segment]
  (update u :path
          #(let [path (or % "/")]
             (str path (if (string/ends-with? path "/")
                         (if (string/starts-with? segment "/") (subs segment 1) segment)       ; path/ + segment
                         (if (string/starts-with? segment "/") segment (str "/" segment))))))) ; path + /segment

(defn url-patient [id] (uri-str (append-path url-patients id)))

(defn get-all-patients [query]
  (parse-json (:body @(request {:url (-> url-patients (assoc-query* query) uri-str)
                                :method :get}))))

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
    (is (= (parse-json (:body @(request {:url (uri-str url-patients) :method :get})))
           {:data [] :count 0})))

  (testing "Get all properly"
    (let [patients (repeat 5 (gen-patient))
          entities (map patient->entity patients)
          enumerated (map-indexed #(merge %2 {:id (inc %1)}) patients)]
      (sql/insert-multi! datasource :patients (-> entities first keys vec) (vec (map vals entities)))
      (is (= (parse-json (:body @(request {:url (uri-str url-patients) :method :get})))
             {:data enumerated :count (count enumerated)}))))

  (testing "Get all in ascending order by id"
    (is (= (map #(:id %) (:data (parse-json (:body @(request {:url (uri-str url-patients) :method :get})))))
           (range 1 6)))))

(deftest patients-get-all-filter
  (let [patients (repeat 5 (gen-patient))
        entities (map patient->entity patients)
        enumerated (map-indexed #(merge %2 {:id (inc %1)}) patients)]
    (sql/insert-multi! datasource :patients (-> entities first keys vec) (vec (map vals entities)))

    (testing "Filter by id"
      (let [id (-> enumerated first :id)
            response (get-all-patients {:id id})
            wanted (filter #(= (:id %) id) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by name"
      (let [name (-> enumerated first :first_name)
            response (get-all-patients {:name name})
            wanted (filter #(= (:first_name %) name) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by name (case insensitive)"
      (let [name (-> enumerated first :first_name)
            response (get-all-patients {:name name})
            wanted (filter #(let [{:keys [first_name middle_name last_name]} %]
                              (re-matches (re-pattern (str "(?i).*" name ".*"))
                                          (str first_name " " middle_name " " last_name))) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by name (partial)"
      (let [name (as-> enumerated $
                   (first $)
                   (:first_name $)
                   (let [len (count $)]
                     (if (>= len 3) (subs $ (/ len 3)) $))) ;; drop the 1/3 part
            response (get-all-patients {:name name})
            wanted (filter #(let [{:keys [first_name middle_name last_name]} %]
                              (re-matches (re-pattern (str "(?i).*" name ".*"))
                                          (str first_name " " middle_name " " last_name))) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by sex"
      (let [sex (-> enumerated first :sex)
            response (get-all-patients {:sex sex})
            wanted (filter #(= (:sex %) sex) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by birth date"
      (let [birth-date (-> enumerated first :birth_date)
            response (get-all-patients {:birth-date birth-date})
            wanted (filter #(= (:birth_date %) birth-date) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by address"
      (let [address (-> enumerated first :address)
            response (get-all-patients {:address address})
            wanted (filter #(= (:address %) address) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by insurance"
      (let [insurance (-> enumerated first :insurance)
            response (get-all-patients {:insurance insurance})
            wanted (filter #(= (:insurance %) insurance) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))

    (testing "Filter by all"
      (let [params (-> enumerated first server/patient->params)
            response (get-all-patients params)
            wanted (filter #(let [{:keys [id first_name middle_name last_name sex birth_date address insurance]} %
                                  name (if (>= (count first_name) 3) (subs first_name (/ (count first_name) 3)) first_name)]
                              (println params)
                              (and (= (:id params) id)
                                   (re-matches (re-pattern (str "(?i).*" name ".*"))
                                               (str first_name " " middle_name " " last_name))
                                   (= (:sex params) sex)
                                   (= (:birth-date params) birth_date)
                                   (= (:address params) address)
                                   (= (:insurance params) insurance))) enumerated)]
        (is (= response {:data wanted :count (count wanted)}))))))

;; POST /patients
(deftest patients-create
  (testing "Create properly"
    (let [patient (gen-patient)]
      @(request {:url (uri-str url-patients)
                 :method :post
                 :body (json/generate-string patient)})
      (is (= (update (jdbc/execute-one! (ds-conf datasource) ["SELECT * FROM patients WHERE id = 1;"]) :birth_date #(.toString %))
             (merge patient {:id 1}))))))

(deftest patients-create-response
  (testing "Return created"
    (let [patient (gen-patient)
          response @(request {:url (uri-str url-patients)
                              :method :post
                              :body (json/generate-string patient)})]
      (is (= (parse-json (:body response))
             (merge patient {:id 1}))))))

(deftest patients-create-invalid
  (testing "Return a proper error response for invalid data"
    (let [invalid (dissoc (gen-patient) :first_name)
          response @(request {:url (uri-str url-patients)
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
      (sql/insert! datasource :patients (patient->entity patient))
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
    (sql/insert! datasource :patients (patient->entity (gen-patient)))
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
    (sql/insert! datasource :patients (patient->entity (gen-patient)))
    (let [response @(request {:url (url-patient 1) :method :delete})]
      (is (= (:status response) 204))
      (is (empty? (:body response))))))
