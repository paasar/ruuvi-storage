(ns ruuvi-storage.handler-test
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.java.io :refer [as-file delete-file]]
            [clojure.string :refer [includes?]]
            [clojure.test :refer :all]
            [cheshire.core :refer [generate-string parse-string]]
            [ring.mock.request :as mock]
            [ruuvi-storage.handler :refer :all]
            [ruuvi-storage.repository :refer [*db-file-name* initiate-db!]]))

(def ^:private measurement-1
  {:name "test"
   :data {:temperature 10 :pressure 1000 :humidity 10}})

(def ^:private measurement-2
  {:name "other test"
   :data {:temperature 20.5 :pressure 1010.5 :humidity 20.5}})

(def ^:private two-measurements
  [measurement-1
   measurement-2])

(defn- delete-db-file []
  (delete-file *db-file-name* true))

(defn with-test-db [test]
  (with-redefs [*db-file-name* "./test.sqlite.db"]
    (delete-db-file)
    (initiate-db!)
    (test)
    (delete-db-file)))

(use-fixtures :each with-test-db)

(defn- post-update [data]
  (app (mock/request :post "/update" data)))

(defn- get-chart-data []
  (app (mock/request :get "/chart-data")))

(def ^:private invalid-data-response
  {:error "Invalid data. Expected something like this: [{\"name\": \"Upstairs\", \"temperature\": 20.0, \"pressure\": 1000.0, \"humidity\": 50.0}]"})

(deftest test-html-rendering-routes
  (testing "main route returns HTML without data"
    (let [{:keys [body headers status]} (app (mock/request :get "/"))]
      (is (= 200 status))
      (is (= "text/html; charset=utf-8" (get headers "Content-Type")))
      (is (= "<html><head><meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\" /><link href=\"./styles.css\" rel=\"stylesheet\" type=\"text/css\" /><script crossorigin=\"anonymous\" integrity=\"sha256-xKeoJ50pzbUGkpQxDYHD7o7hxe0LaOGeguUidbq6vis=\" src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.bundle.min.js\"></script><link crossorigin==\"anonymous\" href=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.css\" integrity=\"sha256-aa0xaJgmK/X74WM224KMQeNQC2xYKwlAt08oZqjeF0E=\" rel=\"stylesheet\" type=\"text/css\" /><script src=\"./loader.js\" type=\"text/javascript\"></script></head><body><div class=\"content\"><div class=\"chart\"><canvas height=\"400\" id=\"measurement-chart\" width=\"400\"></canvas></div></div></body></html>"
             body))))

  (testing "inserted data is returned by main route"
    (post-update (generate-string two-measurements))
    (let [{:keys [body status]} (app (mock/request :get "/"))]
      (is (= 200 status))
      (is (includes? body "<div class=\"tag-name\">test<"))
      (is (includes? body ">10</div><div class=\"item\">1000</div><div class=\"item\">10"))
      (is (includes? body "<div class=\"tag-name\">other test<"))
      (is (includes? body ">20.5</div><div class=\"item\">1010.5</div><div class=\"item\">20.5"))))

  (testing "invalid limit returns 400"
    (let [{:keys [body headers status]} (app (mock/request :get "/?limit=not-a-number"))]
      (is (= 400 status))
      (is (= "text/plain; charset=utf-8" (get headers "Content-Type")))
      (is (= "Optional value limit must be a positive integer and less than 10000." body))))

    (testing "not-found route"
      (let [response (app (mock/request :get "/invalid"))]
        (is (= 404 (:status response))))))

(deftest test-chart-js-json-rendering-route
  (testing "inserted data is returned as chart.js line chart readable json"
    (post-update (generate-string two-measurements))
    (let [{:keys [body headers status]} (get-chart-data)
          [fst snd] (-> body (parse-string ->kebab-case-keyword) :data :datasets)]
      (is (= 200 status))
      (is (= "application/json" (get headers "Content-Type")))
      (is (= "other test" (:label fst)))
      (is (= 20.5 (-> fst :data first :y)))
      (is (= "test" (:label snd)))
      (is (= 10 (-> snd :data first :y)))))

  (testing "invalid limit returns 400"
    (let [{:keys [body headers status]} (app (mock/request :get "/chart-data?limit=-1"))]
      (is (= 400 status))
      (is (= "application/json" (get headers "Content-Type")))
      (is (= "{\"error\":\"Optional value limit must be a positive integer and less than 10000.\"}" body)))))

(deftest storing-data
  (testing "invalid data returns 400"
    (let [{:keys [headers status]} (post-update "notjson")]
      (is (= 400 status))
      (is (= "application/json" (get headers "Content-Type")))))

  (testing "empty data object returns 400"
    (let [{:keys [body status]} (post-update (generate-string {}))]
      (is (= 400 status))
      (is (= (generate-string invalid-data-response) body))))

  (testing "empty array returns 400"
    (let [{:keys [body status]} (post-update (generate-string []))]
      (is (= 400 status))
      (is (= (generate-string invalid-data-response) body))))

  (testing "one valid value is accepted and stored"
    (let [{:keys [body status]} (post-update (generate-string [measurement-1]))
          stored-measurement (-> (get-chart-data) :body (parse-string ->kebab-case-keyword) :data :datasets first)]
      (is (= 200 status))
      (is (= (generate-string {:result "OK" :stored 1}) body))
      (is (= 10 (-> stored-measurement :data first :y)))))

  (testing "two valid values is accepted and stored"
    (let [{:keys [body status]} (post-update (generate-string two-measurements))]
      (is (= 200 status))
      (is (= (generate-string {:result "OK" :stored 2}) body)))))
