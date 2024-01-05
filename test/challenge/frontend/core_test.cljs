(ns challenge.frontend.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing use-fixtures]]
   [re-frame.core :as rf]
   [day8.re-frame.test :as rft]
   [reagent.core :as r]
   [reagent.dom.client :refer [create-root render]]
   [challenge.common.domain :as domain]
   [challenge.frontend.routes :refer [setup-router]]
   [challenge.frontend.events :as events]
   [challenge.frontend.subs :as subs]
   [challenge.frontend.view.index :refer [main-page]]
   [challenge.frontend.view.patients.create :refer [create]]
   [challenge.frontend.view.patients.edit :refer [edit]]
   [challenge.frontend.view.patients.index :refer [table]]
   [challenge.frontend.view.patients.show :refer [show]]
   ["@testing-library/react" :refer [screen] :as rtl]
   ["@testing-library/dom" :refer [within]]))

;;; Utils
(defn with-render [elem f]
  (let [mounted (rtl/render (r/as-element elem))]
    (try (f mounted)
         (finally (rtl/cleanup) (.unmount mounted) (r/flush)))))

(defn change [elem value]
  (.change rtl/fireEvent elem (clj->js {:target {:value value}})))

(defn call [f] (f))

(def form-queries
  {:first-name  #(.getByPlaceholderText screen "First name")
   :middle-name #(.getByPlaceholderText screen "Middle name")
   :last-name   #(.getByPlaceholderText screen "Last name")
   :sex         #(.getByText            screen "Sex")
   :birth-date  #(.getByPlaceholderText screen "Birth date")
   :address     #(.getByPlaceholderText screen "Address")
   :insurance   #(.getByPlaceholderText screen "Insurance")})

;;; Tests
(deftest init
  (testing "Renders correctly"
    (is (nil? (render (create-root (.getElementById js/document "app")) [main-page])))))

(deftest patients-create
  (rft/run-test-sync
   (let [{:keys [first-name middle-name last-name sex birth-date address insurance]} form-queries]
     (testing "Form renders correctly"
       (with-render [create] (fn [_] (is (->> form-queries vals (map call) (every? some?))))))

     (testing "Form elements work correctly"
       (with-render [create]
         (fn [_]
           (change (first-name) "a")
           (change (middle-name) "b")
           (change (last-name) "c")
           (change (address) "d")
           (change (insurance) "e")
           (change (birth-date) "2024-01-01")
           ;; (change (sex) "male")
           ))
       (with-render [create]
         (fn [_]
           (is (and (= "a" @(rf/subscribe [::subs/form :form-patient-create :first_name]))
                    (= "b" @(rf/subscribe [::subs/form :form-patient-create :middle_name]))
                    (= "c" @(rf/subscribe [::subs/form :form-patient-create :last_name]))
                    (= "d" @(rf/subscribe [::subs/form :form-patient-create :address]))
                    (= "e" @(rf/subscribe [::subs/form :form-patient-create :insurance]))
                    (= "2024-01-01" @(rf/subscribe [::subs/form :form-patient-create :birth_date]))
                    ;; (= "male" @(rf/subscribe [::subs/form :form-patient-create :sex]))
                    ))))))))

(deftest patients-edit
  (rft/run-test-sync
   (let [{:keys [first-name middle-name last-name sex birth-date address insurance]} form-queries]
     (testing "Form renders correctly"
       (with-render [edit] (fn [_] (is (->> form-queries vals (map call) (every? some?))))))

     (testing "Form elements work correctly"
       (with-render [edit]
         (fn [_]
           (change (first-name) "a")
           (change (middle-name) "b")
           (change (last-name) "c")
           (change (address) "d")
           (change (insurance) "e")
           (change (birth-date) "2024-01-01")
           ;; (change (sex) "male")
           ))
       (with-render [edit]
         (fn [_]
           (is (and (= "a" @(rf/subscribe [::subs/form :form-patient-update :first_name]))
                    (= "b" @(rf/subscribe [::subs/form :form-patient-update :middle_name]))
                    (= "c" @(rf/subscribe [::subs/form :form-patient-update :last_name]))
                    (= "d" @(rf/subscribe [::subs/form :form-patient-update :address]))
                    (= "e" @(rf/subscribe [::subs/form :form-patient-update :insurance]))
                    (= "2024-01-01" @(rf/subscribe [::subs/form :form-patient-update :birth_date]))
                    ;; (= "male" @(rf/subscribe [::subs/form :form-patient-create :sex]))
                    ))))))))

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
               (.getByPlaceholderText (within tbody) "Insurance"))))))

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
              (.getByText screen "Actions"))))))

(defn create-app-element [tests]
  (.appendChild (.-body js/document)
                (doto (.createElement js/document "div")
                  (.setAttribute "id" "app")
                  (.setAttribute "style" "display:none;")))
  (setup-router)
  (tests))

(use-fixtures :once create-app-element)
(use-fixtures :each (fn [test] (test) (rtl/cleanup)))
