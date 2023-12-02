(ns challenge.frontend.routes
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.events :as events]
   [challenge.frontend.view.patients.update :refer [patient-edit]]))

(def routes
  [["/patients"
    {:name :patients-list
     :view challenge.frontend.view.patients.index/table
     :controllers
     [{:start (fn [] (reframe/dispatch [::events/fetch-patients-list]))}]}]

   ["/patients/create"
    {:name :patient-create
     :view challenge.frontend.view.patients.create/create}]

   ["/patients/show/:id"
    {:name :patient-info
     :view challenge.frontend.view.patients.show/show
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
