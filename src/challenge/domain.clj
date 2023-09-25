(ns challenge.domain
  (:require [clojure.spec.alpha :as s]))

(s/def ::first-name #(and (string? %) (>= 255 (count %))))
(s/def ::middle-name #(and (string? %) (>= 255 (count %))))
(s/def ::last-name #(and (string? %) (>= 255 (count %))))
(s/def ::sex #{"male" "female"})
(s/def ::birth-date #(instance? java.time.LocalDateTime %))
(s/def ::address string?)
(s/def ::insurance string?)
(s/def ::patient
  (s/keys :req-un [::first-name ::middle-name ::last-name ::sex ::birth-date ::address ::insurance]))

(defrecord Patient [first-name middle-name last-name sex birth-date address insurance])
