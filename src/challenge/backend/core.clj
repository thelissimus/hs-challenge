(ns challenge.backend.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [expound.alpha :refer [expound-str]]
   [aero.core :refer [read-config]]
   [next.jdbc.connection :refer [->pool]]
   [org.httpkit.server :refer [run-server]]
   [taoensso.timbre :refer [error info infof]]
   [challenge.backend.lib :refer [conform-let*]]
   [challenge.backend.config :as config]
   [challenge.backend.server :as server])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn -main [& _]
  (conform-let*
   [cfg (read-config (io/resource "config/backend.edn") {:profile :default})
    config (s/conform ::config/config cfg)
    ^HikariDataSource ds (->pool HikariDataSource (:db config))]
   (do
     (info "Connected to database")
     (infof "Running server on http://localhost:%d" (:port config))
     (run-server (server/app ds) {:port (:port config)}))
   (do
     (error (expound-str ::config/config cfg {:print-specs? false}))
     (System/exit 1))))

(comment
  (def cfg (s/conform ::config/config (read-config (io/resource "config/backend.edn") {:profile :default})))
  (def ds (->pool HikariDataSource (:db cfg)))
  (def server (run-server (server/app ds) {:port (:port cfg)}))
  (server)
  (.close ds)
  :rcf)
