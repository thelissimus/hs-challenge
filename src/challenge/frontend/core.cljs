(ns challenge.frontend.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as reframe]
   [reitit.frontend :as rf]
   [reitit.frontend.easy :as rfe]
   [reitit.coercion.spec :as rcs]
   [challenge.frontend.events :as events]
   [challenge.frontend.view :refer [main-page]]
   [challenge.frontend.routes :refer [routes]]))

(defn ^:dev/after-load mount-root []
  (reframe/clear-subscription-cache!)
  (rfe/start! (rf/router routes {:data {:coercion rcs/coercion}})
              (fn [m] (when m (reframe/dispatch [::events/navigated m])))
              {:use-fragment true})
  (let [root (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root)
    (rdom/render [main-page] root)))

(defn init []
  (reframe/dispatch-sync [::events/init-db])
  (mount-root))
