(ns challenge.frontend.view
  (:require [re-frame.core :as re-frame]
            [challenge.frontend.subs :as subs]))

(defn home-page []
  [:h2 "Home"])

(defn patient-row [p]
  [:<>
   [:td (:id p)]
   [:td (str (:first_name p) " " (:middle_name p) " " (:last_name p))]
   [:td (:sex p)]
   [:td (:birth_date p)]
   [:td (:address p)]
   [:td (:insurance p)]])

(defn patients-list []
  [:h2 "Patients list"]
  (let [patients @(re-frame/subscribe [::subs/patients-list])]
    [:table
     [:thead
      [:tr
       [:th "#"]
       [:th "Full name"]
       [:th "Sex"]
       [:th "Birth date"]
       [:th "Address"]
       [:th "Insurance"]]]
     [:tbody
      (for [p patients]
        [:tr {:key (:id p)} (patient-row p)])]]))

(defn patient-info []
  [:h2 "Patient info"])

(defn patient-edit []
  [:h2 "Patient edit"])
