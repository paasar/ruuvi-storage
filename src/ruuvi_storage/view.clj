(ns ruuvi-storage.view
  (:require [hiccup.core :refer [html]]))

(defn- measurement-rows [measurements]
  (for [{:keys [temperature pressure humidity created]} measurements]
    (list [:div.item temperature]
          [:div.item pressure]
          [:div.item humidity]
          [:div.item.timestamp created])))

(defn- measurement-tables [measurements-all]
  (for [[tag-name measurements] measurements-all]
    [:div.measurement-table
     [:div.tag-name tag-name]
     [:div.data-grid
      [:div.header "Temperature"]
      [:div.header "Pressure"]
      [:div.header "Humidity"]
      [:div.header "Created"]
      (measurement-rows measurements)]]))

(defn- measurements-view [measurements-all]
  [:div.content
   (measurement-tables measurements-all)])

(defn main-view [measurements]
  (html
   [:html
    [:head
     [:meta  {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
     [:link {:rel "stylesheet" :type "text/css" :href "./styles.css"}]]
    [:body
     (measurements-view measurements)]]))
