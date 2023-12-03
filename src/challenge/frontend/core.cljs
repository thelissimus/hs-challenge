(ns challenge.frontend.core
  (:require
   [reagent.dom.client :refer [create-root render]]
   [re-frame.core :as reframe]
   [reitit.frontend :refer [router]]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rcs]
   [challenge.frontend.events :as events]
   [challenge.frontend.view.index :refer [main-page]]
   [challenge.frontend.routes :refer [routes]]))

(defn ^:dev/after-load mount-root []
  (reframe/clear-subscription-cache!)
  (rfe/start! (router routes {:data {:coercion rcs/coercion}})
              (fn [m] (when m (reframe/dispatch [::events/navigated m])))
              {:use-fragment true})
  (render (create-root (.getElementById js/document "app")) [main-page]))

(defn init []
  (reframe/dispatch-sync [::events/init-db])
  (mount-root))
