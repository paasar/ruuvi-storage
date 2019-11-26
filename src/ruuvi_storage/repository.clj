(ns ruuvi-storage.repository
  (:require [clojure.java.jdbc :as j]
            [clojure.tools.logging :refer [info]]))

(def ^:dynamic *db-file-name* "./ruuvi-storage.sqlite.db")

(defn db []
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     *db-file-name*})

(defn initiate-db! []
  (info "Initiating database")
  (j/execute! (db) "CREATE TABLE IF NOT EXISTS info (
                      version INTEGER DEFAULT 1)")
  (j/execute! (db) "CREATE TABLE IF NOT EXISTS measurements (
                      id INTEGER PRIMARY KEY,
                      name VARCHAR NOT NULL,
                      temperature INTEGER NOT NULL,
                      pressure INTEGER NOT NULL,
                      humidity INTEGER NOT NULL,
                      created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"))

(defn save! [{tag-name :name {:keys [temperature pressure humidity]} :data}]
  (j/insert! (db) :measurements {:name tag-name
                               :temperature temperature
                               :pressure pressure
                               :humidity humidity}))

(defn- group-by-name [result]
  (->> result
       (group-by :name)
       sort))

(defn measurements [limit]
  {:pre [(and (integer? limit) (pos? limit) (< limit 10000))]}
  (group-by-name (j/query (db) ["SELECT name,
                                        temperature,
                                        pressure,
                                        humidity,
                                        datetime(created, 'localtime') as created
                                 FROM measurements
                                 ORDER BY created DESC
                                 LIMIT ?"
                                limit])))
