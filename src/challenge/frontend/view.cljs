(ns challenge.frontend.view
  (:require [re-frame.core :as re-frame]
            [challenge.frontend.subs :as subs]))

(defn home-page []
  [:h2 "Home"])

(defn patients-list []
  [:h2 "Patients list"]
  (let [patients @(re-frame/subscribe [::subs/patients-list])]
    [:ol
     (for [p patients]
       [:li
        [:p (:first_name p)]])]))

(defn patient-info []
  [:h2 "Patient info"])

(defn patient-edit []
  [:h2 "Patient edit"])
