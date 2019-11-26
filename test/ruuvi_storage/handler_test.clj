(ns ruuvi-storage.handler-test
  (:require [camel-snake-kebab.core :refer [->kebab-case-keyword]]
            [clojure.java.io :refer [as-file delete-file]]
            [clojure.string :refer [includes?]]
            [clojure.test :refer :all]
            [cheshire.core :refer [generate-string parse-string]]
            [ring.mock.request :as mock]
            [ruuvi-storage.handler :refer :all]
            [ruuvi-storage.repository :refer [*db-file-name* initiate-db!]]))

(def ^:private two-measurements
  [{:name "test"
    :data {:temperature 10 :pressure 1000 :humidity 10}}
   {:name "other test"
    :data {:temperature 20 :pressure 1010 :humidity 20}}])

(defn- delete-db-file []
  (delete-file *db-file-name* true))

(defn with-test-db [test]
  (with-redefs [*db-file-name* "./test.sqlite.db"]
    (delete-db-file)
    (initiate-db!)
    (test)
    (delete-db-file)))

(use-fixtures :each with-test-db)

(deftest test-html-rendering-routes
  (testing "main route returns HTML without data"
    (let [response (app (mock/request :get "/"))]
      (is (= 200 (:status response)))
      (is (= "<html><head><meta content=\"width=device-width, initial-scale=1.0\" name=\"viewport\" /><link href=\"./styles.css\" rel=\"stylesheet\" type=\"text/css\" /><script crossorigin=\"anonymous\" integrity=\"sha256-xKeoJ50pzbUGkpQxDYHD7o7hxe0LaOGeguUidbq6vis=\" src=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.bundle.min.js\"></script><link crossorigin==\"anonymous\" href=\"https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.8.0/Chart.min.css\" integrity=\"sha256-aa0xaJgmK/X74WM224KMQeNQC2xYKwlAt08oZqjeF0E=\" rel=\"stylesheet\" type=\"text/css\" /><script src=\"./loader.js\" type=\"text/javascript\"></script></head><body><div class=\"content\"><div class=\"chart\"><canvas height=\"400\" id=\"measurement-chart\" width=\"400\"></canvas></div></div></body></html>"
             (:body response)))))

  (testing "inserted data is returned by main route"
    (app (mock/request :post "/update" (generate-string two-measurements)))
    (let [{:keys [body status]} (app (mock/request :get "/"))]
      (is (= 200 status))
      (is (includes? body "<div class=\"tag-name\">test<"))
      (is (includes? body ">10</div><div class=\"item\">1000</div><div class=\"item\">10"))
      (is (includes? body "<div class=\"tag-name\">other test<"))
      (is (includes? body ">20</div><div class=\"item\">1010</div><div class=\"item\">20"))))

    (testing "not-found route"
      (let [response (app (mock/request :get "/invalid"))]
        (is (= 404 (:status response))))))

(deftest test-chart-js-json-rendering-route
  (testing "inserted data is returned as chart.js line chart readable json"
    (app (mock/request :post "/update" (generate-string two-measurements)))
    (let [{:keys [body headers status]} (app (mock/request :get "/chart-data"))
          [fst snd] (-> body (parse-string ->kebab-case-keyword) :data :datasets)]
      (is (= 200 status))
      (is (= "other test" (:label fst)))
      (is (= 20 (-> fst :data first :y)))
      (is (= "test" (:label snd)))
      (is (= 10 (-> snd :data first :y))))))
