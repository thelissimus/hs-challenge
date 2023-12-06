(ns challenge.backend.domain
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [next.jdbc.types :as types]
   [java-time.api :as time]
   [challenge.common.domain :refer [male female]]))

(s/def ::non-empty-map (s/and map? not-empty))

(s/def ::first_name (s/and string? #(>= 255 (count %))))
(s/def ::middle_name (s/and string? #(>= 255 (count %))))
(s/def ::last_name (s/and string? #(>= 255 (count %))))
(s/def ::sex (s/with-gen
               (s/conformer #(if (contains? #{male female} %)
                               (types/as-other %)
                               ::s/invalid))
               #(gen/elements [male female])))
(s/def ::birth_date (s/with-gen
                      (s/conformer #(try (time/local-date %)
                                         (catch clojure.lang.ExceptionInfo _ ::s/invalid)))
                      #(gen/elements [(time/format "YYYY-MM-dd" (time/local-date))])))
(s/def ::address string?)
(s/def ::insurance string?)
(s/def ::patient
  (s/keys :req-un [::first_name ::middle_name ::last_name ::sex ::birth_date ::address ::insurance]))
(s/def ::patient-partial
  (s/and ::non-empty-map
         #(every? #{:first_name :middle_name :last_name :sex :birth_date :address :insurance} (keys %))
         (s/keys :opt-un [::first_name ::middle_name ::last_name ::sex ::birth_date ::address ::insurance])))
