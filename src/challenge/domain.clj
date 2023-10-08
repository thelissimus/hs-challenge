(ns challenge.domain
  (:require [clojure.spec.alpha :as s]
            [next.jdbc.types :as types]
            [java-time.api :as time])
  (:import (java.time.format DateTimeParseException)))

(s/def ::first_name (s/and string? #(>= 255 (count %))))
(s/def ::middle_name (s/and string? #(>= 255 (count %))))
(s/def ::last_name (s/and string? #(>= 255 (count %))))
(s/def ::sex (s/conformer #(if (contains? #{"male" "female"} %) (types/as-other %) ::s/invalid)))
(s/def ::birth_date (s/conformer #(try (time/local-date-time %)
                                       (catch DateTimeParseException _ ::s/invalid))))
(s/def ::address string?)
(s/def ::insurance string?)
(s/def ::patient
  (s/keys :req-un [::first_name ::middle_name ::last_name ::sex ::birth_date ::address ::insurance]))
(s/def ::patient-partial
  (s/keys :opt-un [::first_name ::middle_name ::last_name ::sex ::birth_date ::address ::insurance]))

(defrecord Patient [first_name middle_name last_name sex birth_date address insurance])
