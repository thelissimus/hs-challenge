(ns challenge.frontend.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]))

(defn app []
  [:div
   [:h1 "Hello, World"]])

(defn init []
  (rd/render [app] (.-body js/document)))
