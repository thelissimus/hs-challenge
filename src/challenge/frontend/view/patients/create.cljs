(ns challenge.frontend.view.patients.create
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.events :as events]
   [challenge.frontend.common.form :refer [input-view select-view]]))

(defn- input [key placeholder attrs]
  [input-view key :form-patient-create placeholder attrs])

(defn create []
  [:form {:on-submit (fn [event] (.preventDefault event))}
   [input :first_name "First name" {:type "text"}]
   [input :middle_name "Middle name" {:type "text"}]
   [input :last_name "Last name" {:type "text"}]
   [select-view
    :sex
    :form-patient-create
    "Sex"
    [{:value "male" :label "Male"}
     {:value "female" :label "Female"}]]
   [input :birth_date "Birth date" {:type "date"}]
   [input :address "Address" {:type "text"}]
   [input :insurance "Insurance" {:type "text"}]
   [:button {:type "button"
             :on-click #(reframe/dispatch [::events/save-form-patient-create])} "Create"]])
