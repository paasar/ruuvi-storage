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

(defn- t-y-pairs [data-type measurements]
  (for [{:keys [created] :as measurement} measurements]
    {:t created
     :y (data-type measurement)}))

(def ^:private colors ["rgb(240,200,60)"
                       "rgb(200,30,82)"
                       "rgb(44,122,122)"
                       "rgb(20,72,190)"
                       "rgb(100,50,80)"
                       "rgb(190,200,190)"])

(defn- next-color-fn []
  (let [color-cycle (atom colors)]
   (fn []
     (let [color (first @color-cycle)]
       (reset! color-cycle (concat (rest @color-cycle) [color]))
       color))))

(defn chart-data [measurements-all]
  (let [next-color (next-color-fn)]
    {:datasets
     (for [data-type [:temperature :humidity]
           [tag-name measurements] measurements-all]
       {:label (str tag-name " - " (name data-type))
        :yAxisID (name data-type)
        :pointStyle (if (= data-type :temperature) "circle" "triangle")
        :data (t-y-pairs data-type measurements)
        :backgroundColor "rgba(0, 0, 0, 0)"
        :borderColor [(next-color)]
        :borderWidth 1})}))
