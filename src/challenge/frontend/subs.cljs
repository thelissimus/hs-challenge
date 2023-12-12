(ns challenge.frontend.subs
  (:require
   [re-frame.core :as reframe]))

(reframe/reg-sub
 ::db
 identity)

(reframe/reg-sub
 ::current-route
 (fn [db] (:current-route db)))

(reframe/reg-sub
 ::patients-list
 (fn [db] (:patients-list db)))

(reframe/reg-sub
 ::patient-current
 (fn [db] (:patient-current db)))

(reframe/reg-sub
 ::form
 (fn [db [_ form id]] (get-in db [form id])))
