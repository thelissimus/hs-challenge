(ns challenge.frontend.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing use-fixtures]]
   [re-frame.core :as rf]
   [day8.re-frame.test :as rft]
   [reagent.dom.client :refer [create-root render]]
   [challenge.frontend.events :as events]
   [challenge.frontend.subs :as subs]))

(deftest init
  (testing "Renders correctly"
    (render (create-root (.getElementById js/document "app")) [])
    (is true)))

(deftest db-initial-state
  (rft/run-test-sync
   (rf/dispatch [::events/init-db])
   (is (= @(rf/subscribe [::subs/db])
          {:current-route nil
           :patients-list []
           :patients-query nil
           :patient-current nil
           :form-patient-create nil
           :form-patient-update nil}))))

(defn create-app-element [tests]
  (.appendChild (.-body js/document)
                (doto (.createElement js/document "div")
                  (-> (.setAttribute "id" "app"))
                  (-> (.setAttribute "style" "display:none;"))))
  (tests))

(use-fixtures :once create-app-element)
