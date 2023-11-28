(ns challenge.backend.lib
  (:require
   [clojure.spec.alpha :as s]
   [cheshire.core :as json]))

(defmacro conform-let
  {:clj-kondo/lint-as 'clojure.core/let}
  [[sym expr] conseq alt]
  `(let [~sym ~expr]
     (if (s/invalid? ~sym) ~alt ~conseq)))

(defmacro conform-let*
  {:clj-kondo/lint-as 'clojure.core/let}
  [[sym expr & rest :as bindings] conseq alt]
  (if (seq bindings)
    `(conform-let [~sym ~expr]
                  (conform-let* ~(vec rest) ~conseq ~alt)
                  ~alt)
    conseq))

(defn parse-json [s]
  (json/parse-string-strict s true))

(defn parse-json-stream [s]
  (json/parse-stream-strict s true))
