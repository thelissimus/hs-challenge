(ns challenge.frontend.routes
  (:require
   [re-frame.core :as reframe]
   [reitit.frontend :refer [router]]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rcs]
   [challenge.frontend.events :as events]
   [challenge.frontend.view.patients.create :refer [create]]
   [challenge.frontend.view.patients.edit :refer [edit]]
   [challenge.frontend.view.patients.index :refer [table]]
   [challenge.frontend.view.patients.show :refer [show]]))

(def routes
  [["/patients"
    {:name :patients-list
     :view table
     :controllers
     [{:start (fn [] (reframe/dispatch [::events/fetch-patients-list]))}]}]

   ["/patients/create"
    {:name :patient-create
     :view create}]

   ["/patients/show/:id"
    {:name :patient-info
     :view show
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (reframe/dispatch [::events/fetch-patient-current (:id path)]))}]}]

   ["/patients/edit/:id"
    {:name :patient-edit
     :view edit
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (reframe/dispatch [::events/fetch-patient-current-update (:id path)]))}]}]])

(defn setup-router []
  (rfe/start! (router routes {:data {:coercion rcs/coercion}})
              (fn [m] (when m (reframe/dispatch [::events/navigated m])))
              {:use-fragment true}))
