(ns challenge.frontend.view.patients.edit
  (:require
   [re-frame.core :as reframe]
   [reitit.frontend.easy :as rfe]
   [challenge.frontend.events :as events]
   [challenge.frontend.common.form :refer [input-view select-view]]))

(defn- input [key placeholder attrs]
  [input-view key :form-patient-update placeholder attrs])

(defn edit []
  (let [input-attrs {:class "form-input mb-4 w-full"}
        select-attrs {:class "form-select mb-4 w-full"}
        button-class "mb-4 w-full px-4 py-2 rounded"
        primary-btn-attrs {:class (str "btn btn-primary bg-green-400 hover:bg-green-600 text-white" button-class)}
        danger-btn-attrs {:class (str "btn btn-danger bg-red-400 hover:bg-red-600 text-white" button-class)}]
    [:form.form.mx-auto.w-full.max-w-md.flex.flex-col.items-start
     {:on-submit #(.preventDefault %)}
     [input :first_name "First name" input-attrs]
     [input :middle_name "Middle name" input-attrs]
     [input :last_name "Last name" input-attrs]
     [select-view
      :sex
      :form-patient-update
      "Sex"
      [{:value "male" :label "Male"}
       {:value "female" :label "Female"}]
      select-attrs]
     [input :birth_date "Birth date" (merge {:type "date"} input-attrs)]
     [input :address "Address" input-attrs]
     [input :insurance "Insurance" input-attrs]
     [:button
      (merge primary-btn-attrs
             {:type "button"
              :on-click #(reframe/dispatch [::events/save-form-patient-update])})
      "Update"]
     [:button
      (merge danger-btn-attrs
             {:type "button"
              :on-click #(do (reframe/dispatch [::events/delete-patient])
                             (rfe/push-state :patients-list))})
      "Delete"]]))
