(ns challenge.frontend.core
  (:require
   [reagent.dom.client :refer [create-root render]]
   [re-frame.core :as reframe]
   [challenge.frontend.events :as events]
   [challenge.frontend.routes :refer [setup-router]]
   [challenge.frontend.view.index :refer [main-page]]))

(defn ^:dev/after-load mount-root []
  (reframe/clear-subscription-cache!)
  (setup-router)
  (render (create-root (.getElementById js/document "app")) [main-page]))

(defn init []
  (reframe/dispatch-sync [::events/init-db])
  (mount-root))
