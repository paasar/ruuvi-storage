(ns ruuvi-storage.view
  (:require [hiccup.core :refer [html]]))

(defn- head []
  [:head
   [:meta  {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:link {:rel "stylesheet" :type "text/css" :href "./styles.css"}]
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.bundle.min.js"
             :crossorigin "anonymous"
             :integrity "sha256-xKeoJ50pzbUGkpQxDYHD7o7hxe0LaOGeguUidbq6vis="}]
   [:link {:rel "stylesheet"
           :type "text/css"
           :href "https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.css"
           :crossorigin="anonymous"
           :integrity "sha256-aa0xaJgmK/X74WM224KMQeNQC2xYKwlAt08oZqjeF0E="}]
   [:script {:type "text/javascript" :src "./loader.js"}]])

(defn- chart []
  [:div.chart
   [:canvas#measurement-chart {:width "400" :height "400"}]])

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
   (chart)
   (measurement-tables measurements-all)])

(defn main-view [measurements-all]
  (html
   [:html
    (head)
    [:body
     (measurements-view measurements-all)]]))

(defn- t-y-pairs [measurements]
  (for [{:keys [temperature created]} measurements]
    {:t created
     :y temperature}))

(def ^:private colors ["rgb(200,30,82)"
                       "rgb(24,102,102)"
                       "rgb(20,72,190)"
                       "rgb(130,130,10)"])

(defn chart-data [measurements-all]
  {:datasets
   (for [[[tag-name measurements] color] (map vector measurements-all (cycle colors))]
      {:label tag-name
       :data (t-y-pairs measurements)
       :borderColor [color]
       :borderWidth 1})})
