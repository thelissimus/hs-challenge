(ns challenge.frontend.events
  (:require
   [re-frame.core :as reframe]
   [superstructor.re-frame.fetch-fx]
   [reitit.frontend.controllers :refer [apply-controllers]]
   [challenge.frontend.env :refer [config]]
   [challenge.frontend.lib :refer [clj->json]]))

;;; state

(reframe/reg-event-db
 ::init-db
 (fn [_ _]
   {:current-route nil
    :patients-list []
    :patients-query nil
    :patient-current nil
    :form-patient-create nil
    :form-patient-update nil}))

;;; routing

(reframe/reg-event-db
 ::navigated
 (fn [db [_ newm]]
   (let [oldm (:current-route db)
         cs   (apply-controllers (:controllers oldm) newm)]
     (assoc db :current-route (assoc newm :controllers cs)))))

;;; form utils

(reframe/reg-event-db
 ::update-form
 (fn [db [_ form key val]]
   (assoc-in db [form key] val)))

;;; patients

;; GET patients/
(reframe/reg-event-db
 ::fetch-patients-list-ok
 (fn [db [_ {:keys [body]}]]
   (assoc db :patients-list (:data body))))

(reframe/reg-event-db
 ::fetch-patients-list-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::fetch-patients-list
 (fn [{:keys [db]} _]
   (let [params (:patients-query db)
         query (js/URLSearchParams.)]
     (when-let [id (:id params)] (.append query "id" id))
     (when-let [name (:name params)] (.append query "name" name))
     (when-let [sex (:sex params)] (.append query "sex" sex))
     (when-let [bdl (:birth-date-lower params)] (.append query "birth-date-lower" bdl))
     (when-let [bdu (:birth-date-upper params)] (.append query "birth-date-upper" bdu))
     (when-let [address (:address params)] (.append query "address" address))
     (when-let [insurance (:insurance params)] (.append query "insurance" insurance))
     {:fetch {:method                 :get
              :url                    (str
                                       (:backend-url config)
                                       "/patients"
                                       (if (empty? (.toString query)) "" (str "?" (.toString query))))
              :mode                   :cors
              :timeout                5000
              :response-content-types {#"application/.*json" :json}
              :on-success             [::fetch-patients-list-ok]
              :on-failure             [::fetch-patients-list-err]}})))

;; POST patients/
(reframe/reg-event-db
 ::save-form-patient-create-ok
 (fn [db [_ {:keys [body]}]]
   (println "create" body)
   (dissoc db :form-patient-create)))

(reframe/reg-event-db
 ::save-form-patient-create-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::save-form-patient-create
 (fn [{:keys [db]} _]
   {:fetch {:method                 :post
            :body                   (clj->json (:form-patient-create db))
            :url                    (str (:backend-url config) "/patients")
            :mode                   :cors
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [::save-form-patient-create-ok]
            :on-failure             [::save-form-patient-create-err]}}))

;; GET patients/:id
(reframe/reg-event-db
 ::fetch-patient-current-ok
 (fn [db [_ {:keys [body]}]]
   (assoc db :patient-current body)))

(reframe/reg-event-db
 ::fetch-patient-current-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::fetch-patient-current
 (fn [_ [_ id]]
   {:fetch {:method                 :get
            :url                    (str (:backend-url config) "/patients/" id)
            :mode                   :cors
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [::fetch-patient-current-ok]
            :on-failure             [::fetch-patient-current-err]}}))

(reframe/reg-event-db
 ::fetch-patient-current-update-ok
 (fn [db [_ {:keys [body]}]]
   (assoc db :form-patient-update body)))

(reframe/reg-event-db
 ::fetch-patient-current-update-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::fetch-patient-current-update
 (fn [_ [_ id]]
   {:fetch {:method                 :get
            :url                    (str (:backend-url config) "/patients/" id)
            :mode                   :cors
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [::fetch-patient-current-update-ok]
            :on-failure             [::fetch-patient-current-update-err]}}))

;; PATCH patients/:id
(reframe/reg-event-db
 ::save-form-patient-update-ok
 (fn [db [_ {:keys [body]}]]
   (println "update" body)
   db))

(reframe/reg-event-db
 ::save-form-patient-update-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::save-form-patient-update
 (fn [{:keys [db]} _]
   (let [form (:form-patient-update db)]
     {:fetch {:method                 :patch
              :body                   (clj->json (dissoc form :id))
              :url                    (str (:backend-url config) "/patients/" (:id form))
              :mode                   :cors
              :timeout                5000
              :response-content-types {#"application/.*json" :json}
              :on-success             [::save-form-patient-update-ok]
              :on-failure             [::save-form-patient-update-err]}})))

;; DELETE patients/:id
(reframe/reg-event-db
 ::delete-patient-ok
 (fn [db _]
   (dissoc db :form-patient-update)))

(reframe/reg-event-db
 ::delete-patient-err
 (fn [db _] db))

(reframe/reg-event-fx
 ::delete-patient
 (fn [{:keys [db]} _]
   {:fetch {:method                 :delete
            :url                    (str (:backend-url config)
                                         "/patients/"
                                         (get-in db [:form-patient-update :id]))
            :mode                   :cors
            :timeout                5000
            :response-content-types {#"application/.*json" :json}
            :on-success             [::delete-patient-ok]
            :on-failure             [::delete-patient-err]}}))
