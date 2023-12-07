(ns challenge.frontend.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing use-fixtures]]
   [reagent.dom.client :refer [create-root render]]))

(deftest init
  (testing "Renders correctly"
    (render (create-root (.getElementById js/document "app")) [])
    (is true)))

(defn create-app-element [tests]
  (.appendChild (.-body js/document)
                (doto (.createElement js/document "div")
                  (-> (.setAttribute "id" "app"))
                  (-> (.setAttribute "style" "display:none;"))))
  (tests))

(use-fixtures :once create-app-element)
