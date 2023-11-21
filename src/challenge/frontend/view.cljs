(ns challenge.frontend.view
  (:require [re-frame.core :as reframe]
            [challenge.frontend.events :as events]
            [challenge.frontend.subs :as subs]))

(defn home-page []
  [:h2 "Home"])

(defn patient-row [p tag]
  [:<>
   [tag (:id p)]
   [tag (str (:first_name p) " " (:middle_name p) " " (:last_name p))]
   [tag (:sex p)]
   [tag (:birth_date p)]
   [tag (:address p)]
   [tag (:insurance p)]])

(defn patients-list []
  [:h2 "Patients list"]
  (let [patients @(reframe/subscribe [::subs/patients-list])]
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
        [:tr {:key (:id p)} (patient-row p :td)])]]))

(defn input
  ([id placeholder] (input id placeholder {:type "text"}))
  ([id placeholder attrs]
   (let [value @(reframe/subscribe [::subs/form-patient-create id])]
     [:<>
      [:label {:for id}]
      [:input (->> {:value value
                    :placeholder placeholder
                    :on-change #(reframe/dispatch [::events/update-form-patient-create id (-> % .-target .-value)])}
                   (merge attrs))]])))

(defn select [id placeholder options]
  (let [value @(reframe/subscribe [::subs/form-patient-create id])]
    [:select {:value value
              :on-change #(reframe/dispatch [::events/update-form-patient-create id (-> % .-target .-value)])}
     [:option {:selected "selected" :disabled true :hidden true} placeholder]
     (for [{:keys [value label]} options]
       [:option {:key value :value value} label])]))

(defn patient-create []
  [:<>
   [:h2 "Patient create"]
   [:form {:on-submit (fn [event] (.preventDefault event))}
    [input :first_name "First name"]
    [input :middle_name "Middle name"]
    [input :last_name "Last name"]
    [select :sex "Sex" [{:value "male" :label "Male"}
                        {:value "female" :label "Female"}]]
    [input :birth_date "Birth date" {:type "date"}]
    [input :address "Address"]
    [input :insurance "Insurance"]
    [:button {:type "button"
              :on-click #(reframe/dispatch [::events/save-form-patient-create])} "Submit"]]])

(defn patient-info []
  (let [patient @(reframe/subscribe [::subs/patient-current])]
    [:<>
     [:h2 "Patient info"]
     [patient-row patient :div]]))

(defn patient-edit []
  [:h2 "Patient edit"])
