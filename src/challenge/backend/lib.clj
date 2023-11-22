(ns challenge.backend.lib
  (:require [clojure.spec.alpha :as s]))

(defmacro conform-let
  {:clj-kondo/lint-as 'clojure.core/let}
  [[sym expr] & body]
  `(let [~sym ~expr]
     (when-not (s/invalid? ~sym)
       (do ~@body))))

(defmacro conform-let*
  {:clj-kondo/lint-as 'clojure.core/let}
  [[sym expr & rest :as bindings] & body]
  (if (seq bindings)
    `(conform-let [~sym ~expr] (conform-let* ~(vec rest) ~@body))
    `(do ~@body)))
