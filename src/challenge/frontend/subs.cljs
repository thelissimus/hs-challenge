(ns challenge.frontend.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::current-route
 (fn [db] (:current-route db)))

(re-frame/reg-sub
 ::patients-list
 (fn [db] (:patients-list db)))

(re-frame/reg-sub
 ::patient-current
 (fn [db] (:patient-current db)))

(re-frame/reg-sub
 ::form
 (fn [db [_ form id]] (get-in db [form id])))
