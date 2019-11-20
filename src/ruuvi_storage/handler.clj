(ns ruuvi-storage.handler
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [cheshire.core :as json]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ruuvi-storage.repository :refer [initiate-db! measurements save!]]))

(def ^:private latest (atom []))

(defn- body->json [body]
  (try
    (let [body-str (slurp body)]
      (json/parse-string body-str ->kebab-case-keyword))
    (catch Exception _
      (throw (Exception. (str "Could not parse request body as JSON. Body: " body "."))))))

(defroutes app-routes
  (GET "/" [] (str "Hello Measurements!<br/>" (json/generate-string (measurements))))
  (POST "/update" {:keys [body] :as req}
    (let [json (body->json body)]
      (reset! latest [json])
      (save! json)
      {:status 200
       :body (str "OK, got " json)}))
  (route/not-found "Not Found"))

(def app
  (do (initiate-db!)
      (wrap-defaults
        app-routes
        (assoc-in site-defaults [:security :anti-forgery] false))))
