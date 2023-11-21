(ns challenge.frontend.lib)

(defn clj->json [data]
  (.stringify js/JSON (clj->js data)))
