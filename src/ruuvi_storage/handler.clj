(ns ruuvi-storage.handler
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.tools.logging :refer [error]]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [ruuvi-storage.repository :refer [initiate-db! measurements save!]]
            [ruuvi-storage.view :refer [main-view]]))

(def ^:private latest (atom []))

(defn- body->json [body]
  (try
    (let [body-str (slurp body)]
      (json/parse-string body-str ->kebab-case-keyword))
    (catch Exception _
      (throw (Exception. (str "Could not parse request body as JSON. Body: " body "."))))))

(defn- parse-limit [limit]
  (if (nil? limit)
    30
    (Integer/parseInt limit)))

(defroutes app-routes
  (GET "/" _ (redirect "index.html"))

  (GET "/index.html" [limit] (main-view (measurements (parse-limit limit))))

  (POST "/update" {:keys [body] :as req}
    (let [measurements (body->json body)]
      (reset! latest measurements)
      (doseq [measurement measurements]
        (save! measurement))
      {:status 200
       :body (str "OK, got " (json/generate-string measurements))}))

  (route/resources "resources/public")

  (route/not-found "Not Found"))

(defn- wrap-catch-exceptions [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable t
        (error (format "Unexpected error. Error message: %s" (.getMessage t)) t)
        {:status 500
         :body "Sorry, something went wrong."}))))

(def app
  (do (initiate-db!)
      (-> app-routes
          (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
          wrap-catch-exceptions)))
