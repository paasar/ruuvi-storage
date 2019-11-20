(ns ruuvi-storage.view
  (:require [hiccup.core :refer [html]]))

(defn main-view [measurements]
  (html
   [:ul
    (for [{:keys [name temperature pressure humidity created]} measurements]
      [:li (format "%s, %s, %s, %s, %s" name temperature pressure humidity created)])]))
