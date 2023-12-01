(ns challenge.backend.config
  (:require [clojure.spec.alpha :as s]))

(s/def ::port (s/int-in 1024 (inc 65535)))

(s/def ::ne-string (s/and string? (complement empty?)))
(s/def :db/dbtype #{"postgresql"})
(s/def :db/dbname ::ne-string)
(s/def :db/host ::ne-string)
(s/def :db/username ::ne-string)
(s/def :db/password string?)
(s/def ::db
  (s/keys :req-un [:db/dbtype :db/dbname :db/host :db/username :db/password]))

(s/def ::config
  (s/keys :req-un [::port ::db]))
