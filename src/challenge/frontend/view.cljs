(ns challenge.frontend.view
  (:require
   [re-frame.core :as reframe]
   [reitit.frontend.easy :as rfe]
   [challenge.frontend.events :as events]
   [challenge.frontend.subs :as subs]))

(defn nav-item [route text]
  [:li
   [:a.block.py-2.px-4.rounded.bg-orange-600.hover:bg-orange-700.text-white.font-medium
    {:href (rfe/href route)} text]])

(defn main-page []
  (let [current-route @(reframe/subscribe [::subs/current-route])]
    [:div.container.mx-auto.px-4
     [:ul.flex.items-center.space-x-4.py-4
      [nav-item :patients-list "Patients list"]
      [nav-item :patient-create "Patient create"]]
     (when current-route
       [(-> current-route :data :view)])]))

(defn patient-row [p tag]
  [:<>
   [tag (:id p)]
   [tag (str (:first_name p) " " (:middle_name p) " " (:last_name p))]
   [tag (:sex p)]
   [tag (:birth_date p)]
   [tag (:address p)]
   [tag (:insurance p)]
   [tag [:a {:href (rfe/href :patient-edit {:id (:id p)})} "Edit"]]])

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

(defn input-view [key form placeholder attrs]
  [:<>
   [:label {:for key}]
   [:input (->> {:placeholder placeholder
                 :value       @(reframe/subscribe [::subs/form form key])
                 :on-change   #(reframe/dispatch [::events/update-form
                                                  form
                                                  key
                                                  (-> % .-target .-value)])}
                (merge attrs))]])

(defn input-create
  ([key placeholder]
   [input-create key placeholder {:type "text"}])

  ([key placeholder attrs]
   [input-view key :form-patient-create placeholder attrs]))

(defn input-update
  ([key placeholder]
   [input-update key placeholder {:type "text"}])

  ([key placeholder attrs]
   [input-view key :form-patient-update placeholder attrs]))

(defn select-view [key form placeholder options]
  [:select {:value     @(reframe/subscribe [::subs/form form key])
            :on-change #(reframe/dispatch [::events/update-form
                                           form
                                           key
                                           (-> % .-target .-value)])}
   [:option {:selected "selected" :disabled true :hidden true} placeholder]
   (for [{:keys [value label]} options]
     [:option {:key value :value value} label])])

(defn select-create [key placeholder options]
  [select-view key :form-patient-create placeholder options])

(defn select-update [key placeholder options]
  [select-view key :form-patient-update placeholder options])

(defn patient-create []
  [:<>
   [:h2 "Patient create"]
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
              :on-click #(reframe/dispatch [::events/save-form-patient-create])} "Create"]]])

(defn patient-info []
  (let [patient @(reframe/subscribe [::subs/patient-current])]
    [:<>
     [:h2 "Patient info"]
     [patient-row patient :div]]))

(defn patient-edit []
  [:<>
   [:h2 "Patient edit"]
   [:form {:on-submit (fn [event] (.preventDefault event))}
    [input-update :first_name "First name"]
    [input-update :middle_name "Middle name"]
    [input-update :last_name "Last name"]
    [select-update :sex "Sex" [{:value "male" :label "Male"}
                               {:value "female" :label "Female"}]]
    [input-update :birth_date "Birth date" {:type "date"}]
    [input-update :address "Address"]
    [input-update :insurance "Insurance"]
    [:button {:type "button"
              :on-click #(reframe/dispatch [::events/save-form-patient-update])}
     "Update"]
    [:button {:type "button"
              :on-click #(do (reframe/dispatch [::events/delete-patient])
                             (rfe/push-state :patients-list))}
     "Delete"]]])
