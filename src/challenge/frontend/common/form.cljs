(ns challenge.frontend.common.form
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.events :as events]))

(defn input-view [key form placeholder attrs]
  [:<>
   [:label {:for key}]
   [:input (->> {:placeholder placeholder
                 :name        key
                 :value       (or @(reframe/subscribe [::subs/form form key]) "")
                 :on-change   #(reframe/dispatch [::events/update-form
                                                  form
                                                  key
                                                  (-> % .-target .-value)])}
                (merge attrs))]])

(defn select-view [key form placeholder options attrs]
  [:select (merge {:name      key
                   :value     (or @(reframe/subscribe [::subs/form form key]) js/undefined)
                   :on-change #(reframe/dispatch [::events/update-form
                                                  form
                                                  key
                                                  (-> % .-target .-value)])} attrs)
   [:option {:selected "selected" :disabled true :hidden true} placeholder]
   (for [{:keys [value label]} options]
     [:option {:key value :value value} label])])
