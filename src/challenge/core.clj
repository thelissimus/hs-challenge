(ns challenge.core
  (:gen-class)
  (:require [next.jdbc.connection :refer [->pool]]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :refer [info]]
            [challenge.server :refer [app]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(def ^:private db-spec {:dbtype "postgresql"
                        :dbname "challenge"
                        :host "localhost"
                        :username "postgres"})

(defn -main [& _]
  (let [^HikariDataSource ds (->pool HikariDataSource db-spec)]
    (info "Connected to database")
    (info "Running server on http://localhost:8080/")
    (run-server (app ds) {:port 8080})))
