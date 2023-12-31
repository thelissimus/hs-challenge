(ns challenge.frontend.view.patients.index
  (:require
   [clojure.string :refer [capitalize]]
   [re-frame.core :as reframe]
   [reitit.frontend.easy :as rfe]
   [challenge.common.domain :refer [male female]]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.events :as events]
   [challenge.frontend.common.link :refer [a button]]
   [challenge.frontend.common.form :refer [input-view select-view]]))

(defn- input [key placeholder attrs]
  [input-view key :patients-query placeholder attrs])

(defn table []
  (letfn [(th [text]
            [:th.border.border-slate-600.py-2.px-4.text-center text])
          (td [text]
            [:td.border.border-slate-700.p-2.text-left.first:text-center.last:text-center text])]
    (let [patients @(reframe/subscribe [::subs/patients-list])]
      [:table.w-full.table-auto.border-collapse.border.border-slate-500
       [:thead.bg-orange-600.text-white
        [:tr
         [th "ID"]
         [th "Full name"]
         [th "Sex"]
         [th "Birth date"]
         [th "Address"]
         [th "Insurance"]
         [th "Actions"]]]
       [:tbody
        [:tr
         [td [input :id "ID" {}]]
         [td [input :name "Full name" {}]]
         [td [select-view
              :sex
              :patients-query
              "Sex"
              [{:value male :label "Male"}
               {:value female :label "Female"}]
              {}]]
         [td [input :birth-date "Birth date" {:type "date"}]]
         [td [input :address "Address" {}]]
         [td [input :insurance "Insurance" {}]]
         [td [button {:on-click #(reframe/dispatch [::events/fetch-patients-list])} "Find"]]]
        (for [p patients]
          [:tr.odd:bg-white.even:bg-gray-200.hover:bg-orange-100.hover:cursor-pointer
           {:key (:id p)
            :on-click #(rfe/push-state :patient-info {:id (:id p)})}
           [td (:id p)]
           [td (str (:first_name p) " " (:middle_name p) " " (:last_name p))]
           [td (capitalize (:sex p))]
           [td (:birth_date p)]
           [td (:address p)]
           [td (:insurance p)]
           [td [a {:href (rfe/href :patient-edit {:id (:id p)})} "Edit"]]])]])))
