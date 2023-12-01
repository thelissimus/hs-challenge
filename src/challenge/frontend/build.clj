(ns challenge.frontend.build
  (:require
   [clojure.java.io :as io]
   [aero.core :as aero]
   [shadow.cljs.devtools.config :refer [get-build!]]
   [shadow.cljs.devtools.api :as shadow]
   [challenge.frontend.env :as env]))

(defn release
  ([] (release "default"))
  ([release-flag]
   (shadow/release* (-> (get-build! :browser)
                        (assoc ::release-flag release-flag))
                    {})))

(defn watch
  {:shadow/requires-server true}
  ([] (watch "default"))
  ([release-flag]
   (shadow/watch (-> (get-build! :browser)
                     (assoc ::release-flag release-flag)))))

(defn read-config [release-flag]
  (-> (io/resource "config/frontend.edn")
      (aero/read-config {:profile release-flag})
      (assoc :release-flag release-flag)))

(defn load-config
  {:shadow.build/stages #{:compile-prepare}}
  [{:as build-state
    :keys [shadow.build/config]}]
  (let [app-config (read-config (-> config ::release-flag keyword))]
    (alter-var-root #'env/config (constantly app-config))
    (-> build-state
        (assoc-in [:compiler-options :external-config ::env] app-config))))
