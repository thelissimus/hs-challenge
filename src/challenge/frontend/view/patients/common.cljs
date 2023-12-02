(ns challenge.frontend.view.patients.common
  (:require
   [clojure.string :refer [capitalize]]
   [reitit.frontend.easy :as rfe]))

(defn patient-row [p tag]
  [:<>
   [tag (:id p)]
   [tag (str (:first_name p) " " (:middle_name p) " " (:last_name p))]
   [tag (capitalize (:sex p))]
   [tag (:birth_date p)]
   [tag (:address p)]
   [tag (:insurance p)]
   [tag [:a.py-2.px-4.hover:bg-orange-500 {:href (rfe/href :patient-edit {:id (:id p)})} "Edit"]]])
