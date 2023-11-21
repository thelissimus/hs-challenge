(ns challenge.frontend.events
  (:require [re-frame.core :as reframe]
            [superstructor.re-frame.fetch-fx]
            [reitit.frontend.controllers :refer [apply-controllers]]))

(reframe/reg-event-db
 ::init-db
 (fn [_ _]
   {:current-route nil
    :patients-list []}))

(reframe/reg-event-db
 ::navigated
 (fn [db [_ newm]]
   (let [oldm (:current-route db)
         cs   (apply-controllers (:controllers oldm) newm)]
     (assoc db :current-route (assoc newm :controllers cs)))))

(reframe/reg-event-db
 ::update-form-patient-create
 (fn [db [_ key val]]
   (assoc-in db [:form-patient-create key] val)))

(reframe/reg-event-db
 ::fetch-patients-list-ok
 (fn [db [_ {:keys [body]}]]
   (assoc db :patients-list (:data body))))

(reframe/reg-event-db
 ::fetch-patients-list-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::fetch-patients-list
 (fn [_ _]
   {:fetch {:method :get
            :url                    "http://localhost:8080/patients"
            :mode                   :cors
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [::fetch-patients-list-ok]
            :on-failure             [::fetch-patients-list-err]}}))

(reframe/reg-event-db
 ::save-form-patient-create-ok
 (fn [db [_ {:keys [body]}]]
   (println body)
   db))

(reframe/reg-event-db
 ::save-form-patient-create-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::save-form-patient-create
 (fn [db _]
   {:fetch {:method                 :post
            :body                   (:form-patient-create db)
            :url                    "http://localhost:8080/patients"
            :mode                   :cors
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [::save-form-patient-create-ok]
            :on-failure             [::save-form-patient-create-err]}}))
