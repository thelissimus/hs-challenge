(ns challenge.server
  (:gen-class)
  (:require [compojure.core :refer [defroutes GET POST PUT PATCH DELETE context]]
            [compojure.middleware :refer [wrap-canonical-redirect]]))

(defn patients-get-all [req]
  (println "patients-get-all" req))

(defn patients-add [req]
  (println "patients-add" req))

(defn patients-get [id]
  (println "patients-get" id))

(defn patients-update-partial [id req]
  (println "patients-update-partial" id req))

(defn patients-update [id req]
  (println "patients-update" id req))

(defn patients-delete [req]
  (println "patients-delete" req))

(defroutes routes
  (wrap-canonical-redirect
   (context "/patients" []
     (GET  "/" [] patients-get-all)
     (POST "/" [] patients-add)
     (context "/:id" [id]
       (GET    "/" [] (patients-get id))
       (PUT    "/" req (patients-update id req))
       (PATCH  "/" req (patients-update-partial id req))
       (DELETE "/" [] (patients-delete id))))))
