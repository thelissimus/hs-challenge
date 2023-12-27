(ns challenge.backend.deps
  (:require
   [aero.core :refer [read-config]]
   [expound.alpha :refer [expound-str]]
   [challenge.backend.config :as config]
   [challenge.backend.lib :refer [conform-let*]]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [mount.core :refer [defstate]]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :refer [->pool]]
   [next.jdbc.date-time :refer [read-as-local]]
   [next.jdbc.result-set :refer [as-unqualified-lower-maps]]
   [taoensso.timbre :refer [error]])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn get-configuration [profile]
  (conform-let*
   [config (-> (io/resource "config/backend.edn")
               (read-config {:profile profile}))
    validated (s/conform ::config/config config)]
   validated
   (do
     (error (expound-str ::config/config config {:print-specs? false}))
     (System/exit 1))))

(defn get-datasrouce [spec]
  (let [pool (->pool HikariDataSource spec)]
    (read-as-local)
    (.close (jdbc/get-connection pool))
    (jdbc/with-options pool {:builder-fn as-unqualified-lower-maps})))

(defstate configuration :start (get-configuration :default))
(defstate datasource    :start (get-datasrouce (:db configuration)))
