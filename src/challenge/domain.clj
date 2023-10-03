(ns challenge.domain
  (:require [clojure.spec.alpha :as s]))

(s/def ::first-name (s/and string? #(>= 255 (count %))))
(s/def ::middle-name (s/and string? #(>= 255 (count %))))
(s/def ::last-name (s/and string? #(>= 255 (count %))))
(s/def ::sex #{"male" "female"})
(s/def ::birth-date #(instance? java.time.LocalDateTime %))
(s/def ::address string?)
(s/def ::insurance string?)
(s/def ::patient
  (s/keys :req-un [::first-name ::middle-name ::last-name ::sex ::birth-date ::address ::insurance]))
(s/def ::patient-partial
  (s/keys :opt-un [::first-name ::middle-name ::last-name ::sex ::birth-date ::address ::insurance]))

(defrecord Patient [id first-name middle-name last-name sex birth-date address insurance])
