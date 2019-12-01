(ns ruuvi-storage.test-helpers
  (:require [clojure.java.io :refer [delete-file]]
            [postal.core :refer [send-message]]
            [ruuvi-storage.repository :refer [*db-file-name* initiate-db!]]))

(defn- delete-db-file []
  (delete-file *db-file-name* true))

(defn with-test-db [test]
  (with-redefs [*db-file-name* "./test.sqlite.db"]
    (delete-db-file)
    (initiate-db!)
    (test)
    (delete-db-file)))

(defn with-mock-email [test]
  (with-redefs [send-message (fn [& _] {})]
    (test)))
