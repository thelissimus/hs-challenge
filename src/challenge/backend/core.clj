(ns challenge.backend.core
  (:gen-class)
  (:require
   [mount.core :as mount]
   [org.httpkit.server :refer [run-server]]
   [taoensso.timbre :refer [info infof]]
   [challenge.backend.deps :refer [configuration]]
   [challenge.backend.server :as server]))

(defn -main [& _]
  (mount/start)
  (info "Connected to database")
  (infof "Running server on http://localhost:%d" (:port configuration))
  (run-server server/app {:port (:port configuration)}))
