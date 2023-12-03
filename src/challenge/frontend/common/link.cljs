(ns challenge.frontend.common.link)

(defn a [attrs text]
  [:a.flex.justify-center.py-2.px-4.hover:bg-orange-500 attrs text])
