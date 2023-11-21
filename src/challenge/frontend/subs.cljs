(ns challenge.frontend.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::current-route
 (fn [db] (:current-route db)))

(re-frame/reg-sub
 ::patients-list
 (fn [db] (:patients-list db)))

(re-frame/reg-sub
 ::form-patient-create
 (fn [db [_ id]] (get-in db [:form-patient-create id])))
