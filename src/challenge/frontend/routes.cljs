(ns challenge.frontend.routes
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.events :as events]
   [challenge.frontend.view.patients.create :refer [patient-create]]
   [challenge.frontend.view.patients.index :refer [patients-list]]
   [challenge.frontend.view.patients.show :refer [patient-info]]
   [challenge.frontend.view.patients.update :refer [patient-edit]]))

(def routes
  [["/patients"
    {:name :patients-list
     :view patients-list
     :controllers
     [{:start (fn [] (reframe/dispatch [::events/fetch-patients-list]))}]}]

   ["/patients/create"
    {:name :patient-create
     :view patient-create}]

   ["/patients/show/:id"
    {:name :patient-info
     :view patient-info
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (reframe/dispatch [::events/fetch-patient-current (:id path)]))}]}]

   ["/patients/edit/:id"
    {:name :patient-edit
     :view patient-edit
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (reframe/dispatch [::events/fetch-patient-current-update (:id path)]))}]}]])
