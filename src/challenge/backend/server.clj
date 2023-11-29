(ns challenge.backend.server
  (:require
   [challenge.backend.domain :as domain]
   [challenge.backend.lib :refer [conform-let conform-let*] :as lib]
   [cheshire.core :as json]
   [cheshire.generate :refer [add-encoder]]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [compojure.core :refer [context DELETE GET PATCH POST routes]]
   [compojure.middleware :refer [wrap-canonical-redirect]]
   [java-time.api :as time]
   [jumblerg.middleware.cors :refer [wrap-cors]]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
   [next.jdbc.sql :as sql]))

;;; data

(defrecord ServerError [message])

(s/def ::message (s/and string? #(< 0 (count %))))
(s/def ::server-error (s/keys :req-un [::message]))
(s/def ::string->int (s/conformer #(try (Integer/parseInt %)
                                        (catch NumberFormatException _ ::s/invalid))))
(s/def ::non-empty-map (s/and map? not-empty))

;;; utils

(add-encoder java.time.LocalDate
             (fn [c jsonGen] (.writeString jsonGen (.toString c))))

(defn validation-err [err]
  {:status 400
   :body (json/generate-string err)})

(defn parse-json-body [req]
  (some-> req :body (io/reader :encoding "UTF-8") lib/parse-json-stream))

;;; endpoints

(defn patients-get-all [ds]
  (fn [_]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (let [res (sql/query ds ["SELECT * FROM patients;"])]
             (json/generate-string {:data  (map (fn [a] (update a :birth_date #(time/local-date %))) res)
                                    :count (count res)}))}))

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
      {:status 204})))

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

;;; router

(defn app [ds]
  (let [ds (jdbc/with-options ds {:builder-fn as-unqualified-lower-maps})]
    (routes
     (-> (context "/patients" []
           (GET  "/" []  (patients-get-all ds))
           (POST "/" req
             (conform-let* [body (parse-json-body req)
                            pat (s/conform ::domain/patient body)]
                           ((patients-add ds) pat)
                           (validation-err (->ServerError (s/explain-str ::domain/patient body)))))
           (context "/:id" [id]
             (GET "/" []
               (conform-let [id_ (s/conform ::string->int id)]
                            ((patients-get ds) id_)
                            nil))
             (PATCH "/" req
               (conform-let* [body (parse-json-body req)
                              id_ (s/conform ::string->int id)
                              pat (s/conform (s/and ::domain/patient-partial ::non-empty-map) body)]
                             ((patients-update ds) id_ pat)
                             (validation-err (->ServerError (s/explain-str ::domain/patient-partial body)))))
             (DELETE "/" []
               (conform-let [id (s/conform ::string->int id)]
                            ((patients-delete ds) id)
                            nil))))
         (wrap-canonical-redirect)
         (wrap-cors identity)))))
