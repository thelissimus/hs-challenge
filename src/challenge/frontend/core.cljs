(ns challenge.frontend.core
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [re-frame.core :as re-frame]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rcs]
            [challenge.frontend.view :as view]))

(defonce match (r/atom nil))

(defn current-page []
  [:div
   [:ul
    [:li [:a {:href (rfe/href ::home-page)} "Home page"]]
    [:li [:a {:href (rfe/href ::patients-list)} "Patients list"]]]
   (when @match
     (let [view (:view (:data @match))]
       [view @match]))])

(def routes
  [["/"
    {:name ::home-page
     :view view/home-page}]

   ["/patients"
    {:name ::patients-list
     :view view/patients-list}]

   ["/patients/:id"
    {:name ::patient-info
     :view view/patient-info
     :parameters {:path {:id int?}}}]

   ["/patients/:id/edit"
    {:name ::patient-edit
     :view view/patient-edit
     :parameters {:path {:id int?}}}]])

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (rfe/start! (rf/router routes {:data {:coercion rcs/coercion}})
              (fn [m] (reset! match m))
              {:use-fragment true})
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [current-page] root-el)))

(defn init []
  (mount-root))
