(ns challenge.backend.server
  (:require
   [cheshire.core :as json]
   [cheshire.generate :refer [add-encoder]]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.walk :refer [keywordize-keys]]
   [compojure.core :refer [context DELETE GET PATCH POST routes]]
   [compojure.middleware :refer [wrap-canonical-redirect]]
   [jumblerg.middleware.cors :refer [wrap-cors]]
   [ring.middleware.params :refer [wrap-params]]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [next.jdbc.sql.builder :as builder]
   [challenge.backend.deps :refer [datasource]]
   [challenge.backend.lib :refer [conform-let conform-let*] :as lib]
   [challenge.backend.domain :as domain]))

;;; Data
(defrecord ServerError [message])

(s/def ::message (s/and string? #(< 0 (count %))))
(s/def ::server-error (s/keys :req-un [::message]))
(s/def ::string->int (s/conformer #(try (Integer/parseInt %)
                                        (catch NumberFormatException _ ::s/invalid))))

(s/def ::id (s/and string? ::string->int))
(s/def ::name ::domain/first_name)
(s/def ::birth-date ::domain/birth_date)
(s/def ::patient-query
  (s/and ::domain/non-empty-map
         (s/keys :opt-un [::id ::name ::domain/sex ::birth-date ::domain/address ::domain/insurance])
         #(every? #{:id :name :sex :birth-date :address :insurance} (keys %))))

;;; Utils
(add-encoder java.time.LocalDate
             (fn [c jsonGen] (.writeString jsonGen (.toString c))))

(defn validation-err [message]
  {:status 400
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string (->ServerError message))})

(defn parse-json-body [req]
  (some-> req :body (io/reader :encoding "UTF-8") lib/parse-json-stream))

;;; Endpoints
(defn patients-get-all
  ([]
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body (let [res (sql/query datasource ["SELECT * FROM patients ORDER BY id ASC;"])]
            (json/generate-string {:data res :count (count res)}))})
  ([{:keys [id name sex birth-date address insurance]}]
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body (let [clauses (into {} (remove (comp nil? val)
                                         {:id id
                                          :sex sex
                                          :birth_date birth-date
                                          :address address
                                          :insurance insurance}))
                name-query "CONCAT(first_name, ' ', middle_name, ' ', last_name) ILIKE '%' || ? || '%'"
                query
                (if (empty? clauses)
                  [(str "SELECT * FROM patients WHERE " name-query) name]
                  (if name
                    (conj (builder/for-query
                           :patients
                           clauses
                           {:suffix (str "AND " name-query " ORDER BY id ASC;")}) name)
                    (builder/for-query :patients clauses {})))
                res (sql/query datasource query)]
            (json/generate-string {:data res :count (count res)}))}))

(defn patients-add
  [patient]
  {:status 201
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string (sql/insert! datasource :patients patient))})

(defn patients-get [id]
  (if-let [res (jdbc/execute-one! datasource ["SELECT * FROM patients WHERE id = ?;" id])]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string res)}
    {:status 404}))

(defn patients-update [id patient]
  (let [count (:next.jdbc/update-count (sql/update! datasource :patients patient {:id id}))]
    (if (zero? count)
      {:status 404}
      {:status 204})))

(defn patients-delete [id]
  (let [count (:next.jdbc/update-count (sql/delete! datasource :patients {:id id}))]
    (if (zero? count)
      {:status 404}
      {:status 204})))

;;; Router
(def app
  (-> (context "/patients" []
        (GET "/" req
          (conform-let [params (s/conform ::patient-query (-> req :params keywordize-keys))]
                       (patients-get-all params)
                       (patients-get-all)))
        (POST "/" req
          (conform-let* [body (parse-json-body req)
                         pat (s/conform ::domain/patient body)]
                        (patients-add pat)
                        (validation-err (s/explain-str ::domain/patient body))))
        (context "/:id" [id]
          (GET "/" []
            (conform-let [id_ (s/conform ::string->int id)]
                         (patients-get id_)
                         nil))
          (PATCH "/" req
            (conform-let* [body (parse-json-body req)
                           id_ (s/conform ::string->int id)
                           pat (s/conform ::domain/patient-partial body)]
                          (patients-update id_ pat)
                          (validation-err (s/explain-str ::domain/patient-partial body))))
          (DELETE "/" []
            (conform-let [id (s/conform ::string->int id)]
                         (patients-delete id)
                         nil))))
      (wrap-canonical-redirect)
      (wrap-params)
      (wrap-cors identity)
      (routes)))
