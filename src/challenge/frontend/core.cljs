(ns challenge.frontend.core
  (:require
   [reagent.dom.client :refer [create-root render]]
   [re-frame.core :as reframe]
   [challenge.frontend.routes :refer [setup-router]]
   [challenge.frontend.view.index :refer [main-page]]))

(defn ^:dev/after-load init []
  (reframe/clear-subscription-cache!)
  (setup-router)
  (render (create-root (.getElementById js/document "app")) [main-page]))
