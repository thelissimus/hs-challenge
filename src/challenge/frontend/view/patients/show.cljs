(ns challenge.frontend.view.patients.show
  (:require
   [re-frame.core :as reframe]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.view.patients.common :refer [patient-view]]))

(defn show []
  (let [patient @(reframe/subscribe [::subs/patient-current])]
    [patient-view patient :div]))
