(ns challenge.frontend.routes
  (:require [re-frame.core :as reframe]
            [challenge.frontend.events :as events]
            [challenge.frontend.view :as view]))

(def routes
  [["/"
    {:name :home-page
     :view view/home-page}]

   ["/patients"
    {:name :patients-list
     :view view/patients-list
     :controllers
     [{:start (fn [& _] (reframe/dispatch [::events/fetch-patients-list]))}]}]

   ["/patients/create"
    {:name :patient-create
     :view view/patient-create}]

   ["/patients/list/:id"
    {:name :patient-info
     :view view/patient-info
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (reframe/dispatch [::events/fetch-patient-current (:id path)]))}]}]

   ["/patients/list/:id/edit"
    {:name :patient-edit
     :view view/patient-edit
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (reframe/dispatch [::events/fetch-patient-current-update (:id path)]))}]}]])
