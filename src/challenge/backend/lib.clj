(ns challenge.backend.lib)

(defmacro conform-let
  {:clj-kondo/lint-as 'clojure.core/let}
  [[sym expr] & body]
  `(let [~sym ~expr]
     (when-not (s/invalid? ~sym)
       (do ~@body))))

(defmacro conform-let*
  {:clj-kondo/lint-as 'clojure.core/let}
  [bindings & body]
  (if (seq bindings)
    `(conform-let [~(first bindings) ~(second bindings)]
                  (conform-let* ~(vec (drop 2 bindings)) ~@body))
    `(do ~@body)))
