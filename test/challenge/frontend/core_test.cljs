(ns challenge.frontend.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing use-fixtures]]
   [re-frame.core :as rf]
   [day8.re-frame.test :as rft]
   [reagent.core :as r]
   [reagent.dom.client :refer [create-root render]]
   [challenge.common.domain :as domain]
   [challenge.frontend.events :as events]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.view.index :refer [main-page]]
   [challenge.frontend.view.patients.create :refer [create]]
   [challenge.frontend.view.patients.edit :refer [edit]]
   [challenge.frontend.view.patients.index :refer [table]]
   [challenge.frontend.view.patients.show :refer [show]]
   ["@testing-library/react" :refer [screen] :as rtl]
   ["@testing-library/dom" :refer [within]]))

(deftest init
  (testing "Renders correctly"
    (is (nil? (render (create-root (.getElementById js/document "app")) [main-page])))))

(deftest db-initial-state
  (rft/run-test-sync
   (testing "Initial db state"
     (rf/dispatch [::events/init-db])
     (is (= @(rf/subscribe [::subs/db])
            {:current-route nil
             :patients-list []
             :patients-query nil
             :patient-current nil
             :form-patient-create nil
             :form-patient-update nil})))))

(deftest patients-create
  (testing "Form renders correctly"
    (rtl/render (r/as-element [create]))
    (is (and (some? (.getByPlaceholderText screen "First name"))
             (some? (.getByPlaceholderText screen "Middle name"))
             (some? (.getByPlaceholderText screen "Last name"))
             (some? (.getByText screen "Sex"))
             (some? (.getByText screen "Male"))
             (some? (.getByText screen "Female"))
             (some? (.getByPlaceholderText screen "Birth date"))
             (some? (.getByPlaceholderText screen "Address"))
             (some? (.getByPlaceholderText screen "Insurance"))))
    (rtl/cleanup)))

(deftest patients-edit
  (testing "Form renders correctly"
    (rtl/render (r/as-element [edit]))
    (is (and (some? (.getByPlaceholderText screen "First name"))
             (some? (.getByPlaceholderText screen "Middle name"))
             (some? (.getByPlaceholderText screen "Last name"))
             (some? (.getByText screen "Sex"))
             (some? (.getByText screen "Male"))
             (some? (.getByText screen "Female"))
             (some? (.getByPlaceholderText screen "Birth date"))
             (some? (.getByPlaceholderText screen "Address"))
             (some? (.getByPlaceholderText screen "Insurance"))))
    (rtl/cleanup)))

(deftest patients-index
  (testing "Page renders correctly"
    (rtl/render (r/as-element [table]))
    (let [thead (.querySelector js/document "thead")
          tbody (.querySelector js/document "tbody")]
      (is (and (.getByText (within thead) "ID")
               (.getByText (within thead) "Full name")
               (.getByText (within thead) "Sex")
               (.getByText (within thead) "Birth date")
               (.getByText (within thead) "Address")
               (.getByText (within thead) "Insurance")
               (.getByText (within thead) "Actions")

               (.getByPlaceholderText (within tbody) "ID")
               (.getByPlaceholderText (within tbody) "Full name")
               (.getByText (within tbody) "Sex")
               (.getByText (within tbody) "Male")
               (.getByText (within tbody) "Female")
               (.getByPlaceholderText (within tbody) "Birth date")
               (.getByPlaceholderText (within tbody) "Address")
               (.getByPlaceholderText (within tbody) "Insurance"))))
    (rtl/cleanup)))

(deftest patients-show
  (rft/run-test-sync
   (testing "Info renders correctly"
     (rf/dispatch [::events/fetch-patient-current-ok
                   {:body {:id 1
                           :first_name ""
                           :middle_name ""
                           :last_name ""
                           :sex domain/male
                           :birth_date "2023-12-18"
                           :address ""
                           :insurance ""}}])
     (rtl/render (r/as-element [show]))
     (is (and (.getByText screen "ID")
              (.getByText screen "Full name")
              (.getByText screen "Sex")
              (.getByText screen "Birth date")
              (.getByText screen "Address")
              (.getByText screen "Insurance")
              (.getByText screen "Actions")))
     (rtl/cleanup))))

(defn create-app-element [tests]
  (.appendChild (.-body js/document)
                (doto (.createElement js/document "div")
                  (.setAttribute "id" "app")
                  (.setAttribute "style" "display:none;")))
  (tests))

(use-fixtures :once create-app-element)
