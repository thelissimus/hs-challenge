(ns challenge.frontend.view.patients.index
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.view.patients.common :refer [patient-row]]))

(defn table []
  (letfn [(th [text]
            [:th.border.border-slate-600.py-2.px-4.text-center text])]
    (let [patients @(reframe/subscribe [::subs/patients-list])]
      [:table.w-full.table-auto.border-collapse.border.border-slate-500
       [:thead.bg-orange-600.text-white
        [:tr
         [th "#"]
         [th "Full name"]
         [th "Sex"]
         [th "Birth date"]
         [th "Address"]
         [th "Insurance"]
         [th "Actions"]]]
       [:tbody
        (for [p patients]
          [:tr.odd:bg-white.even:bg-slate-100.hover:bg-orange-100
           {:key (:id p)}
           (patient-row p :td.border.border-slate-700.p-2.text-left.first:text-center.last:text-center)])]])))
