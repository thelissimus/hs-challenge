(ns challenge.frontend.view.index
  (:require
   [re-frame.core :as reframe]
   [reitit.frontend.easy :as rfe]
   [challenge.frontend.subs :as subs]))

(defn main-page []
  (letfn [(li [route text]
            [:li [:a.block.py-2.px-4.rounded.bg-orange-600.hover:bg-orange-700.text-white.font-medium
                  {:href (rfe/href route)} text]])]
    (let [current-route @(reframe/subscribe [::subs/current-route])]
      [:div.container.mx-auto.px-4
       [:ul.flex.items-center.space-x-4.py-4
        [li :patients-list "Patients list"]
        [li :patient-create "Patient create"]]
       (when current-route
         [(-> current-route :data :view)])])))
