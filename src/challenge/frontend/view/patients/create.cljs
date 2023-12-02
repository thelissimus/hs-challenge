(ns challenge.frontend.view.patients.create
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.events :as events]
   [challenge.frontend.common.form :refer [input-view select-view]]))

(defn input-create
  ([key placeholder]
   [input-create key placeholder {:type "text"}])

  ([key placeholder attrs]
   [input-view key :form-patient-create placeholder attrs]))

(defn select-create [key placeholder options]
  [select-view key :form-patient-create placeholder options])

(defn patient-create []
  [:form {:on-submit (fn [event] (.preventDefault event))}
   [input-create :first_name "First name"]
   [input-create :middle_name "Middle name"]
   [input-create :last_name "Last name"]
   [select-create :sex "Sex" [{:value "male" :label "Male"}
                              {:value "female" :label "Female"}]]
   [input-create :birth_date "Birth date" {:type "date"}]
   [input-create :address "Address"]
   [input-create :insurance "Insurance"]
   [:button {:type "button"
             :on-click #(reframe/dispatch [::events/save-form-patient-create])} "Create"]])
