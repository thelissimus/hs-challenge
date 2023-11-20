(ns challenge.frontend.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as reframe]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rcs]
            [challenge.frontend.view :as view]
            [challenge.frontend.subs :as subs]
            [challenge.frontend.events :as events]))

(defn main-page []
  (let [current-route @(reframe/subscribe [::subs/current-route])]
    [:div
     [:ul
      [:li [:a {:href (rfe/href ::home-page)} "Home page"]]
      [:li [:a {:href (rfe/href ::patients-list)} "Patients list"]]
      [:li [:a {:href (rfe/href ::patient-create)} "Patient create"]]]
     (when current-route
       [(-> current-route :data :view)])]))

(def routes
  [["/"
    {:name ::home-page
     :view view/home-page}]

   ["/patients"
    {:name ::patients-list
     :view view/patients-list
     :controllers
     [{:start (fn [& _] (reframe/dispatch [::events/fetch-patients-list]))}]}]

   ["/patients/create"
    {:name ::patient-create
     :view view/patient-create}]

   ["/patients/list/:id"
    {:name ::patient-info
     :view view/patient-info
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (println (:id path)))}]}]

   ["/patients/list/:id/edit"
    {:name ::patient-edit
     :view view/patient-edit
     :parameters {:path {:id int?}}
     :controllers
     [{:parameters {:path [:id]}
       :start (fn [{:keys [path]}] (println (:id path)))}]}]])

(defn ^:dev/after-load mount-root []
  (reframe/clear-subscription-cache!)
  (rfe/start! (rf/router routes {:data {:coercion rcs/coercion}})
              (fn [m] (when m (reframe/dispatch [::events/navigated m])))
              {:use-fragment true})
  (let [root (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root)
    (rdom/render [main-page] root)))

(defn init []
  (reframe/dispatch-sync [::events/init-db])
  (mount-root))
