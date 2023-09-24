(ns challenge.server
  (:gen-class)
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
            [compojure.core :refer [GET POST PUT PATCH DELETE context routes]]
            [compojure.middleware :refer [wrap-canonical-redirect]]
            [cheshire.core :as json]))

(defn patients-get-all [ds]
  (fn [_] {:status 200
           :headers {"Content-Type" "application/json"}
           :body (let [res (sql/query ds
                                      ["SELECT * FROM patients;"]
                                      {:builder-fn as-unqualified-lower-maps})]
                   (json/generate-string {:data res :count (count res)}))}))

(defn patients-add [ds]
  (fn [req] {:status 201
             :headers {"Content-Type" "application/json"}
             :body (sql/insert! ds :patients req)}))

(defn patients-get [ds]
  (fn [id] {:status 200
            :headers {"Content-Type" "application/json" "F" "T"}
            :body (sql/get-by-id ds
                                 :patients
                                 (Integer/parseInt id)
                                 {:builder-fn as-unqualified-lower-maps})}))

(defn patients-update-partial [ds]
  (fn [id req] {:status 202
                :headers {"Content-Type" "application/json"}
                :body (sql/update! ds
                                   :patients
                                   req
                                   {:id (Integer/parseInt id)}
                                   {:builder-fn as-unqualified-lower-maps})}))

(defn patients-update [ds]
  (fn [id req] {:status 202
                :headers {"Content-Type" "application/json"}
                :body (sql/update! ds :patients req {:id (Integer/parseInt id)})}))

(defn patients-delete [ds]
  (fn [id] {:status 200
            :headers {"Content-Type" "application/json"}
            :body (sql/delete! ds :patients {:id (Integer/parseInt id)})}))

(defn app [ds]
  (routes
   (wrap-canonical-redirect
    (context "/patients" []
      (GET  "/" [] (patients-get-all ds))
      (POST "/" [] (patients-add ds))
      (context "/:id" [id]
        (GET    "/" []  ((patients-get ds) id))
        (PUT    "/" req ((patients-update ds) id req))
        (PATCH  "/" req ((patients-update-partial ds) id req))
        (DELETE "/" []  ((patients-delete ds) id)))))))
