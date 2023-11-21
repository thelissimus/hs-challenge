(ns challenge.backend.server
  (:gen-class)
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
            [next.jdbc.sql :as sql]
            [jumblerg.middleware.cors :refer [wrap-cors]]
            [compojure.core :refer [GET POST PATCH DELETE context routes]]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [cheshire.core :as json]
            [challenge.backend.domain :as domain]
            [challenge.backend.lib :refer [conform-let conform-let*]]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))

(s/def ::string->int (s/conformer #(try (Integer/parseInt %)
                                        (catch NumberFormatException _ ::s/invalid))))

(defn patients-get-all [ds]
  (fn [_]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (let [res (sql/query ds ["SELECT * FROM patients WHERE deleted_at IS NULL;"])]
             (json/generate-string {:data res :count (count res)}))}))

(defn patients-add [ds]
  (fn [patient]
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (sql/insert! ds :patients patient))}))

(defn patients-get [ds]
  (fn [id]
    (if-let [res (jdbc/execute-one! ds ["SELECT * FROM patients WHERE id = ? AND deleted_at IS NULL;" id])]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string res)}
      {:status 204})))

(defn patients-update [ds]
  (fn [id patient]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (sql/update! ds :patients patient {:id id}))}))

(defn patients-delete [ds]
  (fn [id]
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/generate-string (sql/delete! ds :patients {:id id}))}))

(defn app [ds]
  (let [ds (jdbc/with-options ds {:builder-fn as-unqualified-lower-maps})]
    (routes
     (-> (context "/patients" []
           (GET  "/" []  (patients-get-all ds))
           (POST "/" req (conform-let [pat (s/conform ::domain/patient
                                                      (-> (:body req)
                                                          (io/reader :encoding "UTF-8")
                                                          (json/parse-stream true)))]
                                      ((patients-add ds) pat)))
           (context "/:id" [id]
             (GET    "/" []  (conform-let  [id (s/conform ::string->int id)] ((patients-get ds) id)))
             (PATCH  "/" req (conform-let* [id (s/conform ::string->int id)
                                            pat (s/conform ::domain/patient-partial
                                                           (-> (:body req)
                                                               (io/reader :encoding "UTF-8")
                                                               (json/parse-stream true)))]
                                           ((patients-update ds) id pat)))
             (DELETE "/" []  (conform-let [id (s/conform ::string->int id)] ((patients-delete ds) id)))))
         (wrap-canonical-redirect)
         (wrap-cors identity)))))
