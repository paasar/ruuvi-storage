(ns ruuvi-storage.view
  (:require [hiccup.core :refer [html]]))

(defn- measurements-view [measurements]
  [:div.container
   [:div.header "Location"]
   [:div.header "Temperature"]
   [:div.header "Pressure"]
   [:div.header "Humidity"]
   [:div.header "Created"]
   (for [{:keys [name temperature pressure humidity created]} measurements]
     (list [:div.item name]
           [:div.item temperature]
           [:div.item pressure]
           [:div.item humidity]
           [:div.item created]))])

(defn main-view [measurements]
  (html
   [:html
    [:head
     [:meta  {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:link {:rel "stylesheet" :type "text/css" :href "./styles.css"}]]
    [:body
     (measurements-view measurements)]]))
