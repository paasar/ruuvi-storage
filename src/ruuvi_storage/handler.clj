(ns ruuvi-storage.handler
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ruuvi-storage.repository :refer [initiate-db! measurements save!]]
            [ruuvi-storage.view :refer [main-view]]))

(def ^:private latest (atom []))

(defn- body->json [body]
  (try
    (let [body-str (slurp body)]
      (json/parse-string body-str ->kebab-case-keyword))
    (catch Exception _
      (throw (Exception. (str "Could not parse request body as JSON. Body: " body "."))))))

(defroutes app-routes
  (GET "/" [limit] (main-view (measurements :limit (or limit 30))))
  (POST "/update" {:keys [body] :as req}
    (let [measurements (body->json body)]
      (reset! latest measurements)
      (doseq [measurement measurements]
        (save! measurement))
      {:status 200
       :body (str "OK, got " (json/generate-string measurements))}))
  (route/not-found "Not Found"))

(def app
  (do (initiate-db!)
      (wrap-defaults
        app-routes
        (assoc-in site-defaults [:security :anti-forgery] false))))
