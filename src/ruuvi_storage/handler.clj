(ns ruuvi-storage.handler
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.tools.logging :refer [error]]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]
            [ruuvi-storage.repository :refer [initiate-db! measurements save!]]
            [ruuvi-storage.schema :refer [explain valid-measurements?]]
            [ruuvi-storage.view :refer [chart-data main-view]]))

(def ^:private bad-limit-message
  "Optional value limit must be a positive integer and less than 10000.")

(defn- body->json [body-str]
  (try
    (let [measurements (json/parse-string body-str ->kebab-case-keyword)]
      (when-not (valid-measurements? measurements)
        (throw (Exception. (str "Invalid measurements. " (explain measurements)))))
      {:measurements measurements})
    (catch Exception e
      (error e (str "Could not parse request body as JSON. Body: " body-str "."))
      {:error "Invalid data. Expected something like this: [{\"name\": \"Upstairs\", \"temperature\": 20.0, \"pressure\": 1000.0, \"humidity\": 50.0}]"})))

(defn- parse-limit [limit]
  (if (or (nil? limit) (empty? limit))
    30
    (try
      (let [parsed-limit (Integer/parseInt limit)]
        (when (and (pos? parsed-limit) (< parsed-limit 10000))
          parsed-limit))
      (catch Exception _
        (error (str "Invalid limit '" limit "'."))))))

(defn- json-response [status body]
  {:status status
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string body)})

(defroutes app-routes
  (GET "/" [limit]
    (if-let [valid-limit (parse-limit limit)]
      {:status 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (main-view (measurements valid-limit))}
      {:status 400
       :headers {"Content-Type" "text/plain; charset=utf-8"}
       :body bad-limit-message}))

  (GET "/chart-data" [limit]
    (if-let [valid-limit (parse-limit limit)]
      (json-response 200 (-> (measurements valid-limit)
                             chart-data
                             ((fn [data] {:data data}))))
      (json-response 400 {:error bad-limit-message})))

  (POST "/update" {:keys [body] :as req}
    (let [{:keys [measurements error]} (body->json (slurp body))]
      (if error
        (json-response 400 {:error error})
        (do
          (doseq [measurement measurements]
            (save! measurement))
          (json-response 200 {:result "OK"
                              :stored (count measurements)})))))

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
