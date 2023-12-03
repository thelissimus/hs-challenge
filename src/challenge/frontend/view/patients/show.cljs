(ns challenge.frontend.view.patients.show
  (:require
   [clojure.string :refer [capitalize]]
   [re-frame.core :as reframe]
   [reitit.frontend.easy :as rfe]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.common.link :refer [a]]))

(defn show []
  (letfn [(th [text]
            [:th.border.border-slate-600.py-2.px-4.text-left.bg-orange-600.text-white {:class "w-1/3"} text])
          (td [text]
            [:td.border.border-slate-700.p-2.text-left text])]
    (let [patient @(reframe/subscribe [::subs/patient-current])]
      (when patient
        [:table.table-auto.border-collapse.border.border-slate-500
         [:tr
          [th "ID"]
          [td (:id patient)]]
         [:tr
          [th "Full name"]
          [td (str (:first_name patient) " " (:middle_name patient) " " (:last_name patient))]]
         [:tr
          [th "Sex"]
          [td (capitalize (:sex patient))]]
         [:tr
          [th "Birth date"]
          [td (:birth_date patient)]]
         [:tr
          [th "Address"]
          [td (:address patient)]]
         [:tr
          [th "Insurance"]
          [td (:insurance patient)]]
         [:tr
          [th "Actions"]
          [td [a {:href (rfe/href :patient-edit {:id (:id patient)})} "Edit"]]]]))))
