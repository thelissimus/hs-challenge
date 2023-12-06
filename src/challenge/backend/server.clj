(ns challenge.backend.server
  (:require
   [challenge.backend.domain :as domain]
   [challenge.backend.lib :refer [conform-let conform-let*] :as lib]
   [cheshire.core :as json]
   [cheshire.generate :refer [add-encoder]]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.walk :refer [keywordize-keys]]
   [compojure.core :refer [context DELETE GET PATCH POST routes]]
   [compojure.middleware :refer [wrap-canonical-redirect]]
   [java-time.api :as time]
   [jumblerg.middleware.cors :refer [wrap-cors]]
   [ring.middleware.params :refer [wrap-params]]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
   [next.jdbc.date-time :refer [read-as-local]]
   [next.jdbc.sql :as sql]
   [next.jdbc.sql.builder :as builder]))

;;; Data
(defrecord ServerError [message])

(s/def ::message (s/and string? #(< 0 (count %))))
(s/def ::server-error (s/keys :req-un [::message]))
(s/def ::string->int (s/conformer #(try (Integer/parseInt %)
                                        (catch NumberFormatException _ ::s/invalid))))

(s/def ::id (s/and string? ::string->int))
(s/def ::name ::domain/first_name)
(s/def ::patient-query-params
  (s/and ::domain/non-empty-map
         (s/keys :opt-un [::id ::name ::domain/sex ::domain/birth_date ::domain/address ::domain/insurance])
         #(every? #{:id :name :sex :birth_date :address :insurance} (keys %))))

(defn params->patient [{:keys [birth-date] :as params}]
  (as-> params $
    (dissoc $ :birth-date)
    (if (empty? birth-date) $ (assoc $ :birth_date birth-date))))

(defn patient->params [{:keys [id first_name middle_name last_name sex birth_date address insurance]}]
  {:id id
   :name (str first_name " " middle_name " " last_name)
   :sex sex
   :birth-date birth_date
   :address address
   :insurance insurance})

;;; Utils
(add-encoder java.time.LocalDate
             (fn [c jsonGen] (.writeString jsonGen (.toString c))))

(defn validation-err [message]
  {:status 400
   :body (json/generate-string (->ServerError message))})

(defn parse-json-body [req]
  (some-> req :body (io/reader :encoding "UTF-8") lib/parse-json-stream))

;;; Endpoints
(defn patients-get-all [ds]
  (fn
    ([]
     {:status 200
      :headers {"Content-Type" "application/json"}
      :body (let [res (sql/query ds ["SELECT * FROM patients ORDER BY id ASC;"])]
              (json/generate-string {:data res :count (count res)}))})
    ([where]
     {:status 200
      :headers {"Content-Type" "application/json"}
      :body (let [clauses (dissoc where :name)
                  name (:name where)
                  name-query "CONCAT(first_name, ' ', middle_name, ' ', last_name) ILIKE '%' || ? || '%'"
                  query
                  (if (empty? clauses)
                    [(str "SELECT * FROM patients WHERE " name-query) (:name where)]
                    (if name
                      (conj (builder/for-query
                             :patients
                             clauses
                             {:suffix (str "AND " name-query " ORDER BY id ASC;")})
                            (:name where))
                      (builder/for-query :patients clauses {})))
                  res (sql/query ds query)]
              (json/generate-string {:data res :count (count res)}))})))

(defn patients-add [ds]
  (fn [patient]
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (sql/insert! ds :patients patient))}))

(defn patients-get [ds]
  (fn [id]
    (if-let [res (jdbc/execute-one! ds ["SELECT * FROM patients WHERE id = ?;" id])]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (-> res
                 (update :birth_date #(time/local-date %))
                 (json/generate-string))}
      {:status 404})))

(defn patients-update [ds]
  (fn [id patient]
    (let [count (:next.jdbc/update-count (sql/update! ds :patients patient {:id id}))]
      (if (zero? count)
        {:status 404}
        {:status 204}))))

(defn patients-delete [ds]
  (fn [id]
    (let [count (:next.jdbc/update-count (sql/delete! ds :patients {:id id}))]
      (if (zero? count)
        {:status 404}
        {:status 204}))))

;;; Router
(defn app [ds]
  (let [ds (jdbc/with-options ds {:builder-fn as-unqualified-lower-maps})]
    (read-as-local)
    (routes
     (-> (context "/patients" []
           (GET "/" req
             (conform-let [params (s/conform ::patient-query-params
                                             (-> req :params keywordize-keys params->patient))]
                          ((patients-get-all ds) params)
                          ((patients-get-all ds))))
           (POST "/" req
             (conform-let* [body (parse-json-body req)
                            pat (s/conform ::domain/patient body)]
                           ((patients-add ds) pat)
                           (validation-err (s/explain-str ::domain/patient body))))
           (context "/:id" [id]
             (GET "/" []
               (conform-let [id_ (s/conform ::string->int id)]
                            ((patients-get ds) id_)
                            nil))
             (PATCH "/" req
               (conform-let* [body (parse-json-body req)
                              id_ (s/conform ::string->int id)
                              pat (s/conform ::domain/patient-partial body)]
                             ((patients-update ds) id_ pat)
                             (validation-err (s/explain-str ::domain/patient-partial body))))
             (DELETE "/" []
               (conform-let [id (s/conform ::string->int id)]
                            ((patients-delete ds) id)
                            nil))))
         (wrap-canonical-redirect)
         (wrap-params)
         (wrap-cors identity)))))
